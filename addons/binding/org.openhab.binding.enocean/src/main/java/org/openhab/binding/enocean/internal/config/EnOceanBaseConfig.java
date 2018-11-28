/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.config;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.EMPTYENOCEANID;

import java.util.List;

import org.eclipse.smarthome.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanBaseConfig {
    public String enoceanId;

    public List<String> receivingEEPId;

    public EnOceanBaseConfig() {
        enoceanId = EMPTYENOCEANID;
    }

    public byte[] getEnOceanId() {
        return HexUtils.hexToBytes(enoceanId);
    }
}
