/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.generacmobilelink.internal.dto;

import java.time.ZonedDateTime;

/**
 * The {@link ApparatusDetail} represents the details of a Generac Apparatus
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ApparatusDetail {
    public int apparatusId;
    public String name;
    public String serialNumber;
    public int apparatusClassification;
    public String panelId;
    public ZonedDateTime activationDate;
    public String deviceType;
    public String deviceSsid;
    public String shortDeviceId;
    public int apparatusStatus;
    public String heroImageUrl;
    public String statusLabel;
    public String statusText;
    public String eCodeLabel;
    public Weather weather;
    public boolean isConnected;
    public boolean isConnecting;
    public boolean showWarning;
    public boolean hasMaintenanceAlert;
    public ZonedDateTime lastSeen;
    public String connectionTimestamp;
    public Address address;
    public Property[] properties;
    public Subscription subscription;
    public boolean enrolledInVpp;
    public boolean hasActiveVppEvent;
    public ProductInfo[] productInfo;
    public boolean hasDisconnectedNotificationsOn;

    public class Weather {
        public Temperature temperature;
        public int iconCode;

        public class Temperature {
            public double value;
            public String unit;
            public int unitType;
        }
    }

    public class Address {
        public String line1;
        public String line2;
        public String city;
        public String region;
        public String country;
        public String postalCode;
    }

    public class Property {
        public String name;
        public String value;
        public int type;
    }

    public class Subscription {
        public int type;
        public int status;
        public boolean isLegacy;
        public boolean isDunning;
    }

    public class ProductInfo {
        public String name;
        public String value;
        public int type;
    }
}
