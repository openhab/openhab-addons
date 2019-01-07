/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.internal.config;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.ANALOG_ITEM;

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
     * @param force Force update of Siemens LOGO! blocks
     */
    public void setThreshold(final Integer threshold) {
        this.threshold = threshold;
    }

    @Override
    public String getChannelType() {
        return ANALOG_ITEM;
    }

}
