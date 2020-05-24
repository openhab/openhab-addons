/**
 * Copyright (c) 2015-2020 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal;

/**
 * The {@link TACmiConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Christian Niessner (marvkis) - Initial contribution
 */
public class TACmiChannelConfigurationAnalog extends TACmiChannelConfiguration {

    /**
     * measurement type
     */
    public int type;

    // required for MAP operations...
    @Override
    public int hashCode() {
        return 31 * output * type;
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || !other.getClass().equals(TACmiChannelConfigurationAnalog.class))
            return false;
        TACmiChannelConfigurationAnalog o = (TACmiChannelConfigurationAnalog) other;
        return this.output == o.output && this.type == o.type;
    }

}
