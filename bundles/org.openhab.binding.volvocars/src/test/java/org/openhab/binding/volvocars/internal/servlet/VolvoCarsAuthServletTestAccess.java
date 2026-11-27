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
package org.openhab.binding.volvocars.internal.servlet;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Gives tests in other packages access to the URL parsing of {@link VolvoCarsAuthServlet}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VolvoCarsAuthServletTestAccess {
    public static Map<String, String> parseQuery(@Nullable String url) {
        return VolvoCarsAuthServlet.parseQuery(url);
    }
}
