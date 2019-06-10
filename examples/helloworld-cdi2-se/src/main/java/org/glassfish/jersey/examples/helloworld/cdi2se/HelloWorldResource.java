/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.cdi2se;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Singleton-scoped resource.
 *
 * @author Petr Bouda
 */
@Singleton
@Path("helloworld")
public class HelloWorldResource {

    @Inject
    private HelloBean helloBean;

    @GET
    @Path("{name}")
    @Produces("text/plain")
    public String getHello(@PathParam("name") String name) {
        return helloBean.hello(name);
    }
}
