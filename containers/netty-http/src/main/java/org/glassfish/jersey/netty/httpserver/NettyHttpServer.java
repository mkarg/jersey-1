package org.glassfish.jersey.netty.httpserver;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerFactory.SslClientAuth;
import org.glassfish.jersey.server.spi.Server;

import io.netty.channel.Channel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;

public final class NettyHttpServer implements Server {

    private final NettyHttpContainer container;

    private final Channel channel;

    NettyHttpServer(final URI uri, final ResourceConfig resourceConfig, final SSLContext sslContext,
            final SslClientAuth sslClientAuth) {
        this.container = new NettyHttpContainer(resourceConfig);
        this.channel = NettyHttpContainerProvider.createServer(uri, this.container,
                "https".equals(uri.getScheme()) ? new JdkSslContext(sslContext, false, nettyClientAuth(sslClientAuth))
                        : null,
                false);
    }

    private static ClientAuth nettyClientAuth(final SslClientAuth sslClientAuth) {
        switch (sslClientAuth) {
        case NEEDED:
            return ClientAuth.REQUIRE;
        case WANTED:
            return ClientAuth.OPTIONAL;
        default:
            return ClientAuth.NONE;
        }
    }

    @Override
    public final NettyHttpContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return ((InetSocketAddress) this.channel.localAddress()).getPort();
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.channel.close().get();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.channel);
    }

}
