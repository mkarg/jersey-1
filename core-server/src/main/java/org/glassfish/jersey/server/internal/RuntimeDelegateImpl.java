/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.server.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.AbstractRuntimeDelegate;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerFactory;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Server;

/**
 * Server-side implementation of JAX-RS {@link javax.ws.rs.ext.RuntimeDelegate}.
 * This overrides the default implementation of
 * {@link javax.ws.rs.ext.RuntimeDelegate} from jersey-common which does not
 * implement
 * {@link #createEndpoint(javax.ws.rs.core.Application, java.lang.Class)}
 * method.
 *
 * @author Jakub Podlesak
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Martin Matula
 */
public class RuntimeDelegateImpl extends AbstractRuntimeDelegate {

    public RuntimeDelegateImpl() {
        super(new MessagingBinders.HeaderDelegateProviders().getHeaderDelegateProviders());
    }

    @Override
    public <T> T createEndpoint(final Application application, final Class<T> endpointType)
            throws IllegalArgumentException, UnsupportedOperationException {
        if (application == null) {
            throw new IllegalArgumentException("application is null.");
        }
        return ContainerFactory.createContainer(endpointType, application);
    }

    @Override
    public JAXRS.Configuration.Builder createConfigurationBuilder() {
        return new JAXRS.Configuration.Builder() {
            private final Map<String, Object> properties = new HashMap<>();

            {
                this.properties.put(JAXRS.Configuration.PROTOCOL, "HTTP");
                this.properties.put(JAXRS.Configuration.HOST, "localhost");
                this.properties.put(JAXRS.Configuration.ROOT_PATH, "/");
                this.properties.put(ServerProperties.HTTP_SERVER_CLASS, Server.class); // Auto-select first provider
            }

            @Override
            public final JAXRS.Configuration.Builder property(final String name, final Object value) {
                this.properties.put(name, value);
                return this;
            }

            @Override
            public final JAXRS.Configuration build() {
                properties.putIfAbsent(JAXRS.Configuration.PORT,
                        "HTTPS".equals(properties.get(JAXRS.Configuration.PROTOCOL)) ? 443 : 80);

                return new JAXRS.Configuration() {
                    @Override
                    public final Object property(final String name) {
                        return properties.get(name);
                    }
                };
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<JAXRS.Instance> bootstrap(final Application application,
            final JAXRS.Configuration configuration) {
        return CompletableFuture.supplyAsync(() -> {
            final String protocol = configuration.protocol();
            final String host = configuration.host();
            final int port = configuration.port();
            final String rootPath = configuration.rootPath();
            final Class<Server> httpServerClass = (Class<Server>) configuration.property(ServerProperties.HTTP_SERVER_CLASS);

            final URI uri = UriBuilder.fromUri(protocol.toLowerCase() + "://" + host).port(port).path(rootPath).build();
            final ResourceConfig rc = ResourceConfig.forApplication(application);
            final Server server = ServerFactory.createServer(httpServerClass, uri, rc);

            return new JAXRS.Instance() {
                @Override
                public final Configuration configuration() {
                    return configuration;
                }

                @Override
                public final CompletionStage<StopResult> stop() {
                    return server.stop().thenApply(nativeResult -> new StopResult() {

                        @Override
                        public final <T> T unwrap(final Class<T> nativeClass) {
                            return nativeClass.cast(nativeResult);
                        }
                    });
                }

                @Override
                public final <T> T unwrap(final Class<T> nativeClass) {
                    return server.unwrap(nativeClass);
                }
            };
        });
    }

}
