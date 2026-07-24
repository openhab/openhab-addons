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
package org.openhab.binding.melcloud.internal.home.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One entry of a unit's {@code settings} array, as returned by {@code GET /context} (see ADR-003).
 *
 * <p>
 * Unlike the rest of the mobile BFF's JSON (camelCase), each unit's dynamic state is returned as an array of these
 * {@code {"name": "Power", "value": "False"}} pairs rather than a flat object — {@code name} uses PascalCase and
 * matches the underlying C# property name, {@code value} is always representable as a string even when the
 * underlying value is logically a boolean or number.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeSetting {

    public String name = "";
    public String value = "";
}
