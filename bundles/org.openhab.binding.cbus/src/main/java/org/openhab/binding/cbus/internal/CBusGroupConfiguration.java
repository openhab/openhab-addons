/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.cbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for {@link CBusGroupConfiguration}.
 *
 * @author John Harvey - Initial contribution
 */

@NonNullByDefault
public class CBusGroupConfiguration {

    public int group;

    @Override
    public String toString() {
        return String.format("[group=%d]", group);
    }
}
