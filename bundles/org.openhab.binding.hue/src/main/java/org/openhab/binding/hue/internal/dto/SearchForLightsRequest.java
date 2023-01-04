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
package org.openhab.binding.hue.internal.dto;

import java.util.List;

/**
 *
 * @author Q42 - Initial contribution
 * @author Andre Fuechsel - search for lights with given serial number added
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
public class SearchForLightsRequest {
    private List<String> deviceid;

    public SearchForLightsRequest(List<String> deviceid) {
        if (deviceid != null && (deviceid.isEmpty() || deviceid.size() > 16)) {
            throw new IllegalArgumentException("Group cannot be empty and cannot have more than 16 lights");
        }
        if (deviceid != null) {
            this.deviceid = deviceid;
        }
    }
}
