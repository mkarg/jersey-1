package org.glassfish.jersey.simple;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.spi.Server;

public final class SimpleHttpServer implements Server {

    private final SimpleContainer container;

    private final SimpleServer simpleServer;

    SimpleHttpServer(final Application application, final JAXRS.Configuration configuration) {
        final String protocol = configuration.protocol();
        final String host = configuration.host();
        final int port = configuration.port();
        final String rootPath = configuration.rootPath();
        final SSLContext sslContext = configuration.sslContext();
        final JAXRS.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();
        final URI uri = UriBuilder.fromUri(protocol.toLowerCase() + "://" + host).port(port).path(rootPath).build();

        this.container = new SimpleContainer(application);
        this.simpleServer = SimpleContainerFactory.create(uri, "HTTPS".equals(protocol) ? sslContext : null,
                sslClientAuthentication, this.container);
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
