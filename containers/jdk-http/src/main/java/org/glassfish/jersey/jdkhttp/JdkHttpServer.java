package org.glassfish.jersey.jdkhttp;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Server;

import com.sun.net.httpserver.HttpServer;

public final class JdkHttpServer implements Server {

    private final HttpServer httpServer;

    JdkHttpServer(final URI uri, final ResourceConfig resourceConfig) {
        this.httpServer = JdkHttpServerFactory.createHttpServer(uri, resourceConfig);
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.runAsync(() -> {
            this.httpServer.stop(0);
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
