package org.glassfish.jersey.server.spi;

import java.util.concurrent.CompletionStage;

public interface Server {

    public CompletionStage<?> stop();

    public <T> T unwrap(Class<T> nativeClass);

}
