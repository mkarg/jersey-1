package org.glassfish.jersey.netty.httpserver;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Server;

import io.netty.channel.Channel;

public final class NettyHttpServer implements Server {

    private final Channel channel;

    NettyHttpServer(final URI uri, final ResourceConfig resourceConfig) {
        this.channel = NettyHttpContainerProvider.createServer(uri, resourceConfig, false);
    }

    @Override
    public final CompletionStage<?> stop() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.channel.close().get();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.channel);
    }

}
