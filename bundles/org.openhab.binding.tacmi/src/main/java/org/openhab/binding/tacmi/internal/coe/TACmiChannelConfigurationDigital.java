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
public class TACmiChannelConfigurationDigital extends TACmiChannelConfiguration {

    /**
     * initial value
     */
    public @Nullable Boolean initialValue;

    // required for MAP operations...
    @Override
    public int hashCode() {
        Boolean iv = initialValue;
        return 31 * output * (iv == null ? 1 : iv.booleanValue() ? 9 : 3);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !other.getClass().equals(TACmiChannelConfigurationDigital.class)) {
            return false;
        }
        TACmiChannelConfigurationDigital o = (TACmiChannelConfigurationDigital) other;
        return this.output == o.output && Objects.equals(this.initialValue, o.initialValue);
    }
}
