/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
