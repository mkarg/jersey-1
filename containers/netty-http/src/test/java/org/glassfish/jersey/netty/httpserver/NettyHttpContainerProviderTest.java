/*
 * Copyright (c) 2019 Markus KARG. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.netty.httpserver;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.spi.ContainerProvider;
import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Unit tests for {@link NettyHttpContainerProvider}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.29.2
 */
public final class NettyHttpContainerProviderTest {

    @Test(timeout = 15000)
    public final void shouldServeResourceOnRootPath() throws InterruptedException, ExecutionException, URISyntaxException {
        // given
        final ContainerProvider containerProvider = new NettyHttpContainerProvider();
        final Resource resource = new Resource();
        final Application application = new Application() {
            @Override
            public final Set<Object> getSingletons() {
                return Collections.singleton(resource);
            }
        };
        final IntFunction<URI> uri = port -> UriBuilder.fromUri("").scheme("http").host("localhost").port(port).path("rootPort").build();

        // when
        final NettyHttpContainer container = containerProvider.createContainer(NettyHttpContainer.class, application);
        final ServerBootstrap server = createServerBootstrap(uri.apply(getPort()), container, null);
        final Channel channel = startServer(getPort(), container, server, false);
        final int actualPort = ((InetSocketAddress) channel.localAddress()).getPort();
        final String entity = ClientBuilder.newClient().target(uri.apply(actualPort)).request().get(String.class);
        channel.close().get();

        // then
        assertThat(entity, is(resource.toString()));
    }

    @Path("/")
    protected static final class Resource {
        @GET
        @Override
        public final String toString() {
            return super.toString();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(NettyHttpContainerProviderTest.class.getName());

    private static final int DEFAULT_PORT = 0;

    private static final int getPort() {
        final String value = AccessController.doPrivileged(PropertiesHelper.getSystemProperty("jersey.config.test.container.port"));
        if (value != null) {
            try {
                final int i = Integer.parseInt(value);
                if (i < 0) {
                    throw new NumberFormatException("Value is negative.");
                }
                return i;
            } catch (final NumberFormatException e) {
                LOGGER.log(Level.CONFIG, "Value of 'jersey.config.test.container.port'" + " property is not a valid non-negative integer [" + value + "]."
                        + " Reverting to default [" + DEFAULT_PORT + "].", e);
            }
        }

        return DEFAULT_PORT;
    }

    private static final ServerBootstrap createServerBootstrap(final URI baseUri, final NettyHttpContainer container, final SslContext sslContext)
            throws URISyntaxException {
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new JerseyServerInitializer(baseUri, sslContext, container));
        return b;
    }

    private static final Channel startServer(final int port, final NettyHttpContainer container, final ServerBootstrap serverBootstrap, final boolean block)
            throws ProcessingException {
        try {
            final EventLoopGroup bossGroup = serverBootstrap.config().group();
            final EventLoopGroup workerGroup = serverBootstrap.config().childGroup();

            Channel ch = serverBootstrap.bind(port).sync().channel();

            ch.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    container.getApplicationHandler().onShutdown(container);

                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

            if (block)
                ch.closeFuture().sync();
            return ch;
        } catch (final InterruptedException e) {
            throw new ProcessingException(e);
        }
    }

}
