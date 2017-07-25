/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.config;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.ANALOG_NUMBER_CHANNEL;

import java.util.Objects;

/**
 * The {@link PLCLogoAnalogConfiguration} holds configuration of Siemens LOGO! PLC
 * analog input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCLogoAnalogConfiguration extends PLCLogoBlockConfiguration {

    private Integer threshold = 0;
    private String type;

    /**
     * Get Siemens LOGO! device blocks update threshold.
     *
     * @return Configured Siemens LOGO! update threshold
     */
    public Integer getThreshold() {
        return threshold;
    }

    /**
     * Set Siemens LOGO! device blocks update threshold.
     *
     * @param force Force update of Siemens LOGO! device blocks
     */
    public void setThreshold(final Integer threshold) {
        Objects.requireNonNull(threshold, "PLCLogoAnalogConfiguration: Threshold may not be null.");
        this.threshold = threshold;
    }

    /**
     * Get configured item type for channel.
     *
     * @return Configured item type
     */
    public String getType() {
        return type;
    }

    /**
     * Set item type for channel.
     *
     * @param type Item type to configure
     */
    public void setType(final String type) {
        Objects.requireNonNull(threshold, "PLCLogoAnalogConfiguration: Type may not be null.");
        this.type = type;
    }

    @Override
    public boolean isBlockValid() {
        boolean valid = false;
        final String name = getBlockName();
        if (name.length() >= 3) {
            valid = valid || name.startsWith("AI") || name.startsWith("NAI"); // Inputs
            valid = valid || name.startsWith("AQ") || name.startsWith("NAQ"); // Outputs
            valid = valid || name.startsWith("AM"); // Markers
            if (!valid && (name.startsWith("VW") || name.startsWith("VD"))) { // Memory block
                final String[] parts = name.split("\\.");
                valid = (parts.length == 1);
                if (valid && Character.isDigit(parts[0].charAt(2))) {
                    final int address = Integer.parseInt(parts[0].substring(2));
                    valid = (0 <= address) && (address <= (name.startsWith("VD") ? 847 : 849));
                }
            }
        }
        return valid;
    }

    @Override
    public boolean isInputBlock() {
        final String kind = getBlockKind();
        return kind.equalsIgnoreCase("AI") || kind.equalsIgnoreCase("NAI");
    }

    @Override
    public String getItemType() {
        return ANALOG_NUMBER_CHANNEL.equalsIgnoreCase(getType()) ? "Number" : "DateTime";
    }

}
