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

package org.glassfish.jersey.server.spi;

import java.net.URI;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.spi.Contract;

/**
 * Service-provider interface for creating server instances.
 *
 * If supported by the provider, a server instance of the requested Java type
 * will be created.
 * <p>
 * The created server is responsible for listening on a communication chanel for
 * new client requests, dispatching these requests to the registered
 * {@link ApplicationHandler Jersey application handler} using the handler's
 * {@link ApplicationHandler#handle(org.glassfish.jersey.server.ContainerRequest)}
 * handle(requestContext)} method and sending the responses provided by the
 * application back to the client.
 * </p>
 * <p>
 * A provider shall support a one-to-one mapping between a type, provided the
 * type is not {@link Object}. A provider may also support mapping of sub-types
 * of a type (provided the type is not {@code Object}). It is expected that each
 * provider supports mapping for distinct set of types and subtypes so that
 * different providers do not conflict with each other.
 * </p>
 * <p>
 * An implementation can identify itself by placing a Java service provider
 * configuration file (if not already present) -
 * {@code org.glassfish.jersey.server.spi.ServerProvider} - in the resource
 * directory {@code META-INF/services}, and adding the fully qualified
 * service-provider-class of the implementation in the file.
 * </p>
 *
 * @author Markus KARG (markus@headcrashing.eu)
 *
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ServerProvider {

    /**
     * Create a server of a given type which runs the given application defined by
     * the given resource configuration.
     *
     * @param <T>
     *            the type of the server.
     * @param type
     *            the type of the server.
     * @param URI
     *            uri the root address on which to bind the application.
     * @param resourceConfig
     *            The resource configuration defining the application.
     * @return the server, otherwise {@code null} if the provider does not support
     *         the requested {@code type}.
     *
     * @throws ProcessingException
     *             if there is an error creating the server.
     */
    public <T extends Server> T createServer(Class<T> type, URI uri, ResourceConfig resourceConfig)
            throws ProcessingException;
}
