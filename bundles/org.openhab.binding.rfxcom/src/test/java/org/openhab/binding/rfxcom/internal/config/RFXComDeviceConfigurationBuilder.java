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
package org.openhab.binding.rfxcom.internal.config;

/**
 * Test helper for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComDeviceConfigurationBuilder {
    private final RFXComDeviceConfiguration config;

    public RFXComDeviceConfigurationBuilder() {
        config = new RFXComDeviceConfiguration();
    }

    public RFXComDeviceConfigurationBuilder withDeviceId(String deviceId) {
        config.deviceId = deviceId;
        return this;
    }

    public RFXComDeviceConfigurationBuilder withSubType(String subType) {
        config.subType = subType;
        return this;
    }

    public RFXComDeviceConfigurationBuilder withPulse(Integer pulse) {
        config.pulse = pulse;
        return this;
    }

    public RFXComDeviceConfiguration build() {
        return config;
    }
}
