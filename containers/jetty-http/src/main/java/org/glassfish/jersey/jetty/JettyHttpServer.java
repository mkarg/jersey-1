package org.glassfish.jersey.jetty;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Server;

public final class JettyHttpServer implements Server {

    private final org.eclipse.jetty.server.Server httpServer;

    JettyHttpServer(final URI uri, final ResourceConfig resourceConfig) {
        this.httpServer = JettyHttpContainerFactory.createServer(uri, resourceConfig);
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.httpServer.stop();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
