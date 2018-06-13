package org.glassfish.jersey.grizzly2.httpserver;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Server;

public final class GrizzlyHttpServer implements Server {

    private final HttpServer httpServer;

    GrizzlyHttpServer(final URI uri, final ResourceConfig resourceConfig, final SSLContext sslContext,
            final boolean wantsClientAuthentication, final boolean needsClientAuthentication)
            throws NoSuchAlgorithmException, KeyManagementException {
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig,
                "https".equals(uri.getScheme()),
                new SSLEngineConfigurator(sslContext, false, needsClientAuthentication, wantsClientAuthentication));
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
