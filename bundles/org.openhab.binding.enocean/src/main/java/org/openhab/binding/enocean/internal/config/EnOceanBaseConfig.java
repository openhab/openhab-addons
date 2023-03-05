/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.config;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.EMPTYENOCEANID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class EnOceanBaseConfig {
    /**
     * EnOceanId of the physical device
     */
    public String enoceanId;

    /**
     * EEP used/send by physical device
     */
    public List<String> receivingEEPId = new ArrayList<>();
    public boolean receivingSIGEEP = false;

    public EnOceanBaseConfig() {
        enoceanId = EMPTYENOCEANID;
    }

    public byte[] getEnOceanId() {
        return HexUtils.hexToBytes(enoceanId);
    }
}
