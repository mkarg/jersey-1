package org.glassfish.jersey.simple;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerFactory.SslClientAuth;
import org.glassfish.jersey.server.spi.Server;

public final class SimpleHttpServer implements Server {

    private final SimpleContainer container;

    private final SimpleServer simpleServer;

    SimpleHttpServer(final URI uri, final ResourceConfig resourceConfig, final SSLContext sslContext,
            final SslClientAuth sslClientAuth) {
        this.container = new SimpleContainer(resourceConfig);
        this.simpleServer = SimpleContainerFactory.create(uri, "https".equals(uri.getScheme()) ? sslContext : null,
                sslClientAuth, this.container);
    }

    @Override
    public final SimpleContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return this.simpleServer.getPort();
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
