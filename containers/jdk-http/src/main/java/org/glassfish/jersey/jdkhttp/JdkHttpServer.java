package org.glassfish.jersey.jdkhttp;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerFactory.SslClientAuth;
import org.glassfish.jersey.server.spi.Server;

import com.sun.net.httpserver.HttpServer;

public final class JdkHttpServer implements Server {

    private final HttpServer httpServer;

    JdkHttpServer(final URI uri, final ResourceConfig resourceConfig, final SSLContext sslContext,
            final SslClientAuth sslClientAuth) {
        this.httpServer = JdkHttpServerFactory.createHttpServer(uri, new JdkHttpHandlerContainer(resourceConfig),
                sslContext, sslClientAuth, true);
    }

    @Override
    public final int port() {
        return this.httpServer.getAddress().getPort();
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.runAsync(() -> this.httpServer.stop(0));
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
