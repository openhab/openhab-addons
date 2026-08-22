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
package org.openhab.binding.httphook.internal.handler;

import static org.openhab.binding.httphook.internal.servlet.DispatcherServlet.Method.GET;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.httphook.internal.servlet.DispatcherServlet.Method;

/**
 * The {@link EndpointChannelConfiguration} class contains fields mapping response channel configuration parameters.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class EndpointChannelConfiguration {
    public @Nullable Method method = GET;
    public @Nullable List<String> transformationPattern;
    public String contentType = "application/json";

    public EndpointChannelConfiguration() {
    }
}
