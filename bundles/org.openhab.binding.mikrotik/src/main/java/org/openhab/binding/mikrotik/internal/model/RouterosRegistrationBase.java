/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.model;

import static org.openhab.binding.mikrotik.internal.model.RouterosDevice.PROP_ID_KEY;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosRegistrationBase} is a base model class for WiFi client models having casting accessors for
 * data that is same for all WiFi client types.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosRegistrationBase {
    protected Map<String, String> propMap;

    public RouterosRegistrationBase(Map<String, String> props) {
        this.propMap = props;
    }

    public String getId() {
        return propMap.get(PROP_ID_KEY);
    }

    public String getComment() {
        return propMap.get("comment");
    }

    public String getMacAddress() {
        return propMap.get("mac-address");
    }

    public String getSSID() {
        return propMap.get("ssid");
    }
}
