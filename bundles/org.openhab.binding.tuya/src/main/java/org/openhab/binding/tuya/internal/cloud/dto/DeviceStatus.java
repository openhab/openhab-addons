/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.cloud.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceStatus} encapsulates the command and status specification of a device
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class DeviceStatus {
    public String category = "";
    public String productKey = "";
    public List<Description> dpStatusRelationDTOS = List.of();

    @Override
    public String toString() {
        return "DeviceStatus{productKey='" + productKey + "', category='" + category + "', dpStatusRealtionDTOS="
                + dpStatusRelationDTOS + "}";
    }

    public static class Description {
        // public String dpCode = "";
        public int dpId = 0;
        // public String enumMappingMap = "{}";
        public String statusCode = "";
        // public String statusFormat = "{}";
        // public boolean supportLocal = true;
        // public String valueConvert = "default";
        // public String valueDesc = "";
        // public String valueType = "";
        // public String values = "";

        @Override
        public String toString() {
            return "Description{statusCode='" + statusCode + "', dpId=" + dpId + "'}";
        }
    }
}
