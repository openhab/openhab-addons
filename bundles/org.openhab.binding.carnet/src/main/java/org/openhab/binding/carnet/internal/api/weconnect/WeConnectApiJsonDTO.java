/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api.weconnect;

import java.util.ArrayList;

import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiID;

/**
 * {@link CarNetBrandApiID} defines the We Connect API data formats
 *
 * @author Markus Michels - Initial contribution
 */
public class WeConnectApiJsonDTO {
    public static class WCVehicleList {
        public static class WCVehicle {
            /*
             * "vin": "WVWZZZE1ZMP053898",
             * "role": "PRIMARY_USER",
             * "enrollmentStatus": "COMPLETED",
             * "model": "ID.3",
             * "nickname": "ID.3",
             * "capabilities": []
             */

            public static class WCCapability {
                /*
                 * {
                 * "id": "automation",
                 * "expirationDate": "2024-05-09T00:00:00Z",
                 * "userDisablingAllowed": true
                 * }
                 */
                public String id;
                public String expirationDate;
                public Boolean userDisablingAllowed;
            }

            public String vin;
            public String role;
            public String enrollmentStatus;
            public String vehicle;
            public String nickname;
            public ArrayList<WCCapability> capabilities;
        }

        public ArrayList<WCVehicle> data;
    }
}
