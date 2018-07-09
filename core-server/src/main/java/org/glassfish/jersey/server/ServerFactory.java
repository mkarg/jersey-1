/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

package org.glassfish.jersey.server;

import java.net.URI;
import java.rmi.ServerException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;

/**
 * Factory for creating specific HTTP servers.
 *
 * @author Markus KARG (markus@hedcrashing.eu)
 */
public final class ServerFactory {

    /**
     * Prevents instantiation.
     */
    private ServerFactory() {
    }

    /**
     * Create a server according to the class requested.
     * <p>
     * The list of service-provider supporting the {@link ServerProvider}
     * service-provider will be iterated over until one returns a non-null server
     * instance.
     * <p>
     *
     * @param <T>
     *            the type of the server.
     * @param type
     *            the type of the server.
     * @param URI
     *            uri the root address on which to bind the application.
     * @param application
     *            The application to boot.
     * @param sslContext
     *            The secure socket configuration to be used with HTTPS.
     * @param sslClientAuth
     *            Whether the server wants or needs SSL client authentication.
     * @return the server, otherwise {@code null} if the provider does not support
     *         the requested {@code type}.
     * @throws ServerException
     *             if there was an error creating the container.
     * @throws IllegalArgumentException
     *             if no server provider supports the type.
     */
    public static <T extends Server> T createServer(final Class<T> type, final URI uri, final Application application,
            final SSLContext sslContext, final SslClientAuth sslClientAuth) {
        for (final ServerProvider serverProvider : ServiceFinder.find(ServerProvider.class)) {
            final T server = serverProvider.createServer(type, uri, application, sslContext, sslClientAuth);
            if (server != null) {
                return server;
            }
        }

        throw new IllegalArgumentException("No server provider supports the type " + type);
    }

    public static enum SslClientAuth {
        NONE(false, false), WANTED(true, false), NEEDED(false, true);

        private final boolean wanted;

        private final boolean needed;

        private SslClientAuth(final boolean wanted, final boolean needed) {
            this.wanted = wanted;
            this.needed = needed;
        }

        public boolean wanted() {
            return this.wanted;
        }

        public boolean needed() {
            return this.needed;
        }
    }
}
