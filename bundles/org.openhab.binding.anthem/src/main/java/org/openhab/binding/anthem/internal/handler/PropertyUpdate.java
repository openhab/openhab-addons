/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.anthem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PropertyUpdate} class represents a property that need to be set
 * or updated on the Anthem thing.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class PropertyUpdate {
    private String name;
    private String value;

    public PropertyUpdate(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
