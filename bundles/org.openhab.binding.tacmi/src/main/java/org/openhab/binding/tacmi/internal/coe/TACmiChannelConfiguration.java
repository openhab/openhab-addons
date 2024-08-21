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
package org.openhab.binding.tacmi.internal.coe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TACmiConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class TACmiChannelConfiguration {

    /**
     * chnanel / output id
     */
    public int output;

    // required for MAP operations...
    @Override
    public int hashCode() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !other.getClass().equals(TACmiChannelConfiguration.class)) {
            return false;
        }
        TACmiChannelConfiguration o = (TACmiChannelConfiguration) other;
        return this.output == o.output;
    }
}
