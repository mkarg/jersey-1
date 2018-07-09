package org.glassfish.jersey.grizzly2.httpserver;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.server.spi.Server;

public final class GrizzlyHttpServer implements Server {

    private final GrizzlyHttpContainer container;

    private final HttpServer httpServer;

    GrizzlyHttpServer(final URI uri, final Application application, final SSLContext sslContext,
            final boolean wantsClientAuthentication, final boolean needsClientAuthentication)
            throws NoSuchAlgorithmException, KeyManagementException {
        this.container = new GrizzlyHttpContainer(application);
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, this.container,
                "https".equals(uri.getScheme()),
                new SSLEngineConfigurator(sslContext, false, needsClientAuthentication, wantsClientAuthentication),
                true);
    }

    @Override
    public final GrizzlyHttpContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return this.httpServer.getListener("grizzly").getPort();
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.runAsync(this.httpServer::shutdownNow);
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
