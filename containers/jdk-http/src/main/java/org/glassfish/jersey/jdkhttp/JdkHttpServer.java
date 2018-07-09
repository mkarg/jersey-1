package org.glassfish.jersey.jdkhttp;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerFactory.SslClientAuth;
import org.glassfish.jersey.server.spi.Server;

import com.sun.net.httpserver.HttpServer;

public final class JdkHttpServer implements Server {

    private final JdkHttpHandlerContainer container;

    private final HttpServer httpServer;

    JdkHttpServer(final URI uri, final Application application, final SSLContext sslContext,
            final SslClientAuth sslClientAuth) {
        this.container = new JdkHttpHandlerContainer(application);
        this.httpServer = JdkHttpServerFactory.createHttpServer(uri, this.container, sslContext, sslClientAuth, true);
    }

    @Override
    public final JdkHttpHandlerContainer container() {
        return this.container;
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
