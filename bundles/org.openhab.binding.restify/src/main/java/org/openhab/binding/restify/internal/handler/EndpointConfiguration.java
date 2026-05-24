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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method;

/**
 * The {@link EndpointConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class EndpointConfiguration {
    public String path = "/hello-world";
    public @Nullable Method method = GET;
    public @Nullable List<String> transformationPattern = List.of("JS:hello-world.js");
    public @Nullable AuthorizationType authorizationType = AuthorizationType.NONE;
    public String username = "";
    public String password = "";
    public String token = "";
    public String contentType = "application/json";

    public EndpointConfiguration() {
    }

    public EndpointConfiguration(String path, Method method, String transformationPattern) {
        this();
        this.path = path.trim();
        this.method = method;
        this.transformationPattern = List.of(transformationPattern.trim());
    }

    public enum AuthorizationType {
        NONE,
        BASIC,
        BEARER
    }
}
