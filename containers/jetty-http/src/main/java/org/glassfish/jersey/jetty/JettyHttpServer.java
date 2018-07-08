package org.glassfish.jersey.jetty;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerFactory.SslClientAuth;
import org.glassfish.jersey.server.spi.Server;

public final class JettyHttpServer implements Server {

    private final JettyHttpContainer container;

    private final org.eclipse.jetty.server.Server httpServer;

    JettyHttpServer(final URI uri, final ResourceConfig resourceConfig, final SSLContext sslContext,
            final SslClientAuth sslClientAuth) {
        final SslContextFactory sslContextFactory;
        if ("https".equals(uri.getScheme())) {
            sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(sslContext);
            sslContextFactory.setWantClientAuth(sslClientAuth.wanted());
            sslContextFactory.setNeedClientAuth(sslClientAuth.needed());
        } else {
            sslContextFactory = null;
        }
        this.container = ContainerFactory.createContainer(JettyHttpContainer.class, resourceConfig);
        this.httpServer = JettyHttpContainerFactory.createServer(uri, sslContextFactory, this.container, true);
    }

    @Override
    public final JettyHttpContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return ((ServerConnector) this.httpServer.getConnectors()[0]).getPort();
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
