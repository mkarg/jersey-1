package org.glassfish.jersey.jdkhttp;

import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.MANDATORY;
import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.OPTIONAL;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Server;

import com.sun.net.httpserver.HttpServer;

public final class JdkHttpServer implements Server {

    private final JdkHttpHandlerContainer container;

    private final HttpServer httpServer;

    JdkHttpServer(final Application application, final JAXRS.Configuration configuration) {
        final String protocol = configuration.protocol();
        final String host = configuration.host();
        final int port = configuration.port();
        final String rootPath = configuration.rootPath();
        final SSLContext sslContext = configuration.sslContext();
        final JAXRS.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();
        final boolean autoStart = (boolean) configuration.property(ServerProperties.AUTO_START);
        final URI uri = UriBuilder.fromUri(protocol.toLowerCase() + "://" + host).port(port).path(rootPath).build();

        this.container = new JdkHttpHandlerContainer(application);
        this.httpServer = JdkHttpServerFactory.createHttpServer(uri, this.container, sslContext,
                sslClientAuthentication == OPTIONAL, sslClientAuthentication == MANDATORY, autoStart);
    }

    @Override
    public final JdkHttpHandlerContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return this.httpServer.getAddress().getPort();
    }

    @Override
    public final CompletionStage<?> start() {
        return CompletableFuture.runAsync(this.httpServer::start);
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.runAsync(() -> this.httpServer.stop(0));
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
