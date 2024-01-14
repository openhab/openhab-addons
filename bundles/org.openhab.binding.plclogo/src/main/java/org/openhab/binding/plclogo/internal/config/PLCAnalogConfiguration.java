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
package org.openhab.binding.plclogo.internal.config;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.ANALOG_ITEM;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PLCAnalogConfiguration} is a class for configuration
 * of Siemens LOGO! PLC analog input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCAnalogConfiguration extends PLCDigitalConfiguration {

    private Integer threshold = 0;

    /**
     * Get Siemens LOGO! blocks update threshold.
     *
     * @return Configured Siemens LOGO! update threshold
     */
    public Integer getThreshold() {
        return threshold;
    }

    /**
     * Set Siemens LOGO! blocks update threshold.
     *
     * @param threshold
     */
    public void setThreshold(final Integer threshold) {
        this.threshold = threshold;
    }

    @Override
    public String getChannelType() {
        return ANALOG_ITEM;
    }
}
