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
package org.openhab.binding.hue.internal.api.dto.clip1;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Q42 - Initial contribution
 * @author Andre Fuechsel - search for lights with given serial number added
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
public class SearchForLightsRequest {
    @SuppressWarnings("unused")
    private List<String> deviceid;

    public SearchForLightsRequest(List<String> deviceid) {
        if (deviceid.isEmpty() || deviceid.size() > 16) {
            throw new IllegalArgumentException("Group cannot be empty and cannot have more than 16 lights");
        }
        this.deviceid = deviceid;
    }
}
