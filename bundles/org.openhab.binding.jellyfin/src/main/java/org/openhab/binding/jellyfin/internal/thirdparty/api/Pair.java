/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.thirdparty.api;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class Pair {
    private final String name;
    private final String value;

    public Pair(String name, String value) {
        this.name = isValidString(name) ? name : "";
        this.value = isValidString(value) ? value : "";
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    private static boolean isValidString(String arg) {
        return arg != null;
    }
}
