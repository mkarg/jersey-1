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

package org.glassfish.jersey.netty.httpserver;

import java.net.URI;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerFactory.SslClientAuth;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;

/**
 * Server provider for servers based on Netty.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public class NettyHttpServerProvider implements ServerProvider {

    @Override
    public <T extends Server> T createServer(final Class<T> type, final URI uri, final Application application,
            final SSLContext sslContext, final SslClientAuth sslClientAuth) {
        return NettyHttpServer.class == type || Server.class == type
                ? type.cast(new NettyHttpServer(uri, application, sslContext, sslClientAuth))
                : null;
    }
}
