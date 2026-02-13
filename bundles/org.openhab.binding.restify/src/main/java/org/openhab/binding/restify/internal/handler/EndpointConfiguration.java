/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.restify.internal.handler;

import static org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method.GET;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet;

/**
 * The {@link EndpointConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class EndpointConfiguration {
    public String path = "/hello-world";
    public DispatcherServlet.Method method = GET;
    public String endpoint = "{\"response\":{ \"message\":\"Hello World\"}}";

    public EndpointConfiguration() {
    }

    public EndpointConfiguration(String path, DispatcherServlet.Method method, String endpoint) {
        this();
        this.path = path.trim();
        this.method = method;
        this.endpoint = endpoint.trim();
    }
}
