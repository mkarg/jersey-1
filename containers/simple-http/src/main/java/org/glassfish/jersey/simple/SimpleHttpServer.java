package org.glassfish.jersey.simple;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Server;

public final class SimpleHttpServer implements Server {

    private final SimpleServer simpleServer;

    SimpleHttpServer(final URI uri, final ResourceConfig resourceConfig, final SSLContext sslContext) {
        this.simpleServer = SimpleContainerFactory.create(uri, "https".equals(uri.getScheme()) ? sslContext : null,
                resourceConfig);
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.simpleServer.close();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.simpleServer);
    }

}
