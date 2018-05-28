package org.glassfish.jersey.grizzly2.httpserver;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Server;

public final class GrizzlyHttpServer implements Server {

    private final HttpServer httpServer;

    GrizzlyHttpServer(final URI uri, final ResourceConfig resourceConfig) {
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig);
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.runAsync(() -> {
            this.httpServer.shutdownNow();
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
