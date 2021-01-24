/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InterfaceThingConfig} class contains fields mapping thing configuration parameters for
 * network interface things.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class InterfaceThingConfig {
    public String name = "";

    public boolean isValid() {
        return StringUtils.isNotBlank(name);
    }

    @Override
    public String toString() {
        return String.format("InterfaceThingConfig{name=%s}", name);
    }
}
