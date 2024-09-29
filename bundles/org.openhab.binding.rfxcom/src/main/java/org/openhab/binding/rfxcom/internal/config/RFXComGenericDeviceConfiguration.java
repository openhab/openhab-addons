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
package org.openhab.binding.rfxcom.internal.config;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidParameterException;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;

/**
 * Configuration class for generic RFXCOM device.
 *
 * @author Pauli Anttila - Initial contribution
 * @author James Hewitt-Thomas - Add validations and matching
 */
public class RFXComGenericDeviceConfiguration implements RFXComDeviceConfiguration {
    public static final String DEVICE_ID_LABEL = "deviceId";
    public static final String SUB_TYPE_LABEL = "subType";
    public String deviceId;
    public String subType;

    @Override
    public void parseAndValidate() throws RFXComInvalidParameterException {
        if (deviceId == null) {
            throw new RFXComInvalidParameterException("deviceId", null, "RFXCOM device missing deviceId");
        }
        if (subType == null) {
            throw new RFXComInvalidParameterException("subType", null, "RFXCOM device missing subType");
        }
    }

    @Override
    public boolean matchesMessage(RFXComDeviceMessage message) {
        return deviceId.equals(message.getDeviceId());
    }
}
