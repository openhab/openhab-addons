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
package org.openhab.binding.tacmi.internal.coe;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TACmiConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class TACmiChannelConfigurationAnalog extends TACmiChannelConfiguration {

    /**
     * measurement type
     */
    public int type;

    /**
     * initial value
     */
    public @Nullable Double initialValue;

    // required for MAP operations...
    @Override
    public int hashCode() {
        Double iv = initialValue;
        return 31 * output * type * (iv == null ? 1 : iv.hashCode());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !other.getClass().equals(TACmiChannelConfigurationAnalog.class)) {
            return false;
        }
        TACmiChannelConfigurationAnalog o = (TACmiChannelConfigurationAnalog) other;
        return this.output == o.output && this.type == o.type && Objects.equals(this.initialValue, o.initialValue);
    }
}
