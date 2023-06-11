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
package org.openhab.binding.plclogo.internal.config;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PLCDigitalConfiguration} is a base class for configuration
 * of Siemens LOGO! PLC digital input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCDigitalConfiguration extends PLCCommonConfiguration {

    private String kind = "";

    @Override
    public String getBlockKind() {
        return kind;
    }

    /**
     * Set Siemens LOGO! blocks kind.
     * Can be I, Q, M, NI or NQ for digital blocks and
     * AI, AM, AQ, NAI or NAQ for analog
     *
     * @param kind Siemens LOGO! blocks kind
     */
    public void setBlockKind(final String kind) {
        this.kind = kind.trim();
    }

    @Override
    public String getChannelType() {
        boolean isInput = kind.equalsIgnoreCase(I_DIGITAL) || kind.equalsIgnoreCase(NI_DIGITAL);
        return isInput ? DIGITAL_INPUT_ITEM : DIGITAL_OUTPUT_ITEM;
    }
}
