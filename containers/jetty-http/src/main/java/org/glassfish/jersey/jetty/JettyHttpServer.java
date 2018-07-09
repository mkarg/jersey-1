package org.glassfish.jersey.jetty;

import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.MANDATORY;
import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.OPTIONAL;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.spi.Server;

public final class JettyHttpServer implements Server {

    private final JettyHttpContainer container;

    private final org.eclipse.jetty.server.Server httpServer;

    JettyHttpServer(final Application application, final JAXRS.Configuration configuration) {
        final String protocol = configuration.protocol();
        final String host = configuration.host();
        final int port = configuration.port();
        final String rootPath = configuration.rootPath();
        final SSLContext sslContext = configuration.sslContext();
        final JAXRS.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();
        final URI uri = UriBuilder.fromUri(protocol.toLowerCase() + "://" + host).port(port).path(rootPath).build();

        final SslContextFactory sslContextFactory;
        if ("https".equals(uri.getScheme())) {
            sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(sslContext);
            sslContextFactory.setWantClientAuth(sslClientAuthentication == OPTIONAL);
            sslContextFactory.setNeedClientAuth(sslClientAuthentication == MANDATORY);
        } else {
            sslContextFactory = null;
        }
        this.container = ContainerFactory.createContainer(JettyHttpContainer.class, application);
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
