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

package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings lifecycle response
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class LifeCycle {
    public String lifecycle;
    public String excutionId;
    public String appId;
    public String locale;
    public String version;

    public record confirmationData(String appId, String confirmationUrl) {
    }

    public confirmationData confirmationData;

    public record client(String os, String version, String language, String displayMode, String timeZoneOffset,
            String samsungAccountId, String mobileDeviceId) {
    }

    public client client;

    public record configurationData(String installedAppId, String phase, String pageId, String previousPageId) {
    }

    public configurationData configurationData;

    public Data updateData;
    public Data installData;
    public Data executeData;
    public Data eventData;

    public class Data {
        public String authToken;
        public String refreshToken;

        public class InstalledApp {
            public String installedAppId;
            public String locationId;
            public String[] permissions;
            public String[] previousPermissions;
        }

        public class Event {
            public String eventTime;
            public String evnetType;

            public class DeviceEvent {
                public String eventId;
                public String locationId;
                public String ownerId;
                public String ownerType;
                public String deviceId;
                public String componentId;
                public String capability;
                public String attribute;
                public String value;
                public String valueType;
                public Boolean stateChange;
                public String subscriptionName;
            }

            public DeviceEvent deviceEvent;
        }

        public InstalledApp installedApp;
        public Event[] events;
    }
}
