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
package org.openhab.binding.mikrotik.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InterfaceThingConfig} class contains fields mapping thing configuration parameters for
 * network interface things.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class InterfaceThingConfig implements ConfigValidation {
    public String name = "";

    @Override
    public boolean isValid() {
        return !name.isBlank();
    }

    @Override
    public String toString() {
        return String.format("InterfaceThingConfig{name=%s}", name);
    }
}
