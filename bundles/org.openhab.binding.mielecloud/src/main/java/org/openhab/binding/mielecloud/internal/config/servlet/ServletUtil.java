/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.config.servlet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for common servlet tasks.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class ServletUtil {
    private ServletUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the value of a request parameter or returns a default if the parameter is not present.
     */
    public static String getParameterValueOrDefault(HttpServletRequest request, String parameterName,
            String defaultValue) {
        String parameterValue = request.getParameter(parameterName);
        if (parameterValue == null) {
            return defaultValue;
        } else {
            return parameterValue;
        }
    }

    /**
     * Checks whether a request parameter is enabled.
     */
    public static boolean isParameterEnabled(HttpServletRequest request, String parameterName) {
        return "true".equalsIgnoreCase(getParameterValueOrDefault(request, parameterName, "false"));
    }

    /**
     * Checks whether a parameter is present in a request.
     */
    public static boolean isParameterPresent(HttpServletRequest request, String parameterName) {
        String parameterValue = request.getParameter(parameterName);
        return parameterValue != null && !parameterValue.trim().isEmpty();
    }
}
