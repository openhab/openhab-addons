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
package org.openhab.binding.rachio.internal.api.json;

import java.util.ArrayList;

import org.openhab.binding.rachio.internal.api.json.RachioDeviceGson.RachioCloudDevice;

/**
 * {@link RachioApiGson} maps some API results to a Java object (using GSon).
 *
 * @author Markus Michels - Initial contribution
 */
public class RachioApiGson {
    public static class RachioCloudPersonId {
        public String id = ""; // "id":"xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx"
    }

    public static class RachioCloudStatus {
        public long                         createDate = -1;                // "createDate":1494626927000,
        public String                       id         = "";                // "id":"xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx",
        public String                       username   = "";                // "username":"openhab",
        public String                       fullName   = "";                // "fullName":"openHAB user",
        public String                       email      = "";                // "email":info@openhab.info",
        public ArrayList<RachioCloudDevice> devices    = new ArrayList<>(); // "devices":[]
        public boolean                      deleted    = false;             // "deleted":false
    }

    public static class RachioApiWebHookEntry {
        public long   createDate     = -1;
        public long   lastUpdateDate = -1;
        public String id             = "";
        public String url            = "";
        public String externalId     = "";
    }

    public static class RachioApiWebHookList {
        public ArrayList<RachioApiWebHookEntry> webhooks = new ArrayList<>();

    }

    public static class RachioEventProperty {
        public String propertyName = "";
        public String oldValue     = "";
        public String newValue     = "";
    }

    public static class RachioZoneStatus {
        public Integer duration      = 0;
        public String  scheduleType  = "";
        public Integer zoneNumber    = 0;
        public String  executionType = "";
        public String  state         = "";
        public String  startTime     = "";
        public String  endTime       = "";
        // public Integer corId = 0; // currently unused
        // public Integer seqId = 0; // currently unused
        // public Integer ix = 0; // currently unused
    }

    public static class RachioCloudDelta {
        // V3: ZONE_DELTA / SCHEDULE_DELTA
        public String routingId  = ""; // "routingId" : "d3beb3ab-b85a-49fe-a45d-37c4d95ea9a8",
        public String icon       = ""; // "icon" : "NO_ICON",
        public String action     = ""; // "action" : "UPDATED",
        public String zoneId     = ""; // "zoneId" : "e49c8b55-a553-4733-b1cf-0e402b97db49",
        public String externalId = ""; // "externalId" : "cc765dfb-d095-4ceb-8062-b9d88dcce911",
        public String subType    = ""; // "subType" : "ZONE_DELTA",
        public String id         = ""; // "id" : "e9d4fa9f-1619-37c4-b457-3845620643d2",
        public String type       = ""; // "type" : "DELTA",
        public String category   = ""; // "category" : "DEVICE",
        public String deviceId   = ""; // "deviceId" : "d3beb3ab-b85a-49fe-a45d-37c4d95ea9a8",
        public String timestamp  = ""; // "timestamp" : "2018-04-09T23:17:14.365Z"
    }

}
