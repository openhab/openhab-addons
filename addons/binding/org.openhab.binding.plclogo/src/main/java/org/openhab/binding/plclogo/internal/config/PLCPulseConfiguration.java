/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.internal.config;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PLCPulseConfiguration} is a class for configuration
 * of Siemens LOGO! PLC memory input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCPulseConfiguration extends PLCMemoryConfiguration {

    private @Nullable String observe;
    private Integer pulse = 150;

    /**
     * Get observed Siemens LOGO! block name or memory address.
     *
     * @return Observed Siemens LOGO! block name or memory address
     */
    public String getObservedBlock() {
        String result = observe;
        if (result == null) {
            result = getBlockName();
            observe = result;
        }
        return result;
    }

    /**
     * Set Siemens LOGO! block name or memory address to observe.
     *
     * @param name Siemens LOGO! memory block name or memory address
     */
    public void setObservedBlock(final String name) {
        this.observe = name;
    }

    public String getObservedChannelType() {
        final String kind = getObservedBlockKind();
        return (kind.equalsIgnoreCase("I") || kind.equalsIgnoreCase("NI")) ? DIGITAL_INPUT_ITEM : DIGITAL_OUTPUT_ITEM;
    }

    public String getObservedBlockKind() {
        String result = observe;
        if (result == null) {
            result = getBlockName();
            observe = result;
        }
        return getBlockKind(result);
    }

    public Integer getPulseLength() {
        return pulse;
    }

    public void setPulseLength(Integer pulse) {
        this.pulse = pulse;
    }

}
