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
package org.openhab.binding.homeconnectdirect.internal.servlet.routing;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Route definition for servlet routing.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record Route(String method, Pattern path, List<String> consumes, List<String> produces,
        Set<String> pathVariables, boolean secured, RequestHandler handler) {
}
