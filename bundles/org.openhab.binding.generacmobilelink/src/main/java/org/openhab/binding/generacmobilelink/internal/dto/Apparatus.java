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

import java.util.List;

/**
 * The {@link Apparatus} represents a Generac Apparatus (Generator)
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Apparatus {
    public int apparatusId;
    public String serialNumber;
    public String name;
    public int type;
    public String localizedAddress;
    public String materialDescription;
    public String heroImageUrl;
    public int apparatusStatus;
    public boolean isConnected;
    public boolean isConnecting;
    public boolean showWarning;
    public Weather weather;
    public String preferredDealerName;
    public String preferredDealerPhone;
    public String preferredDealerEmail;
    public boolean isDealerManaged;
    public boolean isDealerUnmonitored;
    public String modelNumber;
    public String panelId;
    public List<Property> properties;

    public class Weather {
        public Temperature temperature;
        public int iconCode;

        public class Temperature {
            public double value;
            public String unit;
            public int unitType;
        }
    }

    public class Property {
        public String name;
        public Value value;
        public int type;

        public class Value {
            public int type;
            public String status;
            public boolean isLegacy;
            public boolean isDunning;
            public String deviceId;
            public String deviceType;
            public String signalStrength;
            public String batteryLevel;
        }
    }

    public class Device {
        public String deviceId;
        public String deviceType;
        public String signalStrength;
        public String batteryLevel;
        public String status;
    }
}
