package org.glassfish.jersey.netty.httpserver;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.spi.Server;

import io.netty.channel.Channel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;

public final class NettyHttpServer implements Server {

    private final NettyHttpContainer container;

    private final Channel channel;

    NettyHttpServer(final Application application, final JAXRS.Configuration configuration) {
        final String protocol = configuration.protocol();
        final String host = configuration.host();
        final int port = configuration.port();
        final String rootPath = configuration.rootPath();
        final SSLContext sslContext = configuration.sslContext();
        final JAXRS.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();
        final URI uri = UriBuilder.fromUri(protocol.toLowerCase() + "://" + host).port(port).path(rootPath).build();

        this.container = new NettyHttpContainer(application);
        this.channel = NettyHttpContainerProvider.createServer(uri, this.container,
                "HTTPS".equals(protocol)
                        ? new JdkSslContext(sslContext, false, nettyClientAuth(sslClientAuthentication))
                        : null,
                false);
    }

    private static ClientAuth nettyClientAuth(
            final JAXRS.Configuration.SSLClientAuthentication sslClientAuthentication) {
        switch (sslClientAuthentication) {
        case MANDATORY:
            return ClientAuth.REQUIRE;
        case OPTIONAL:
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
