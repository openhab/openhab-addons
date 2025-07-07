/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HueSyncConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncConstants {

    public static class ENDPOINTS {
        public static final String DEVICE = "device";
        public static final String REGISTRATIONS = "registrations";
        public static final String HDMI = "hdmi";
        public static final String EXECUTION = "execution";

        public static class COMMANDS {
            public static final String MODE = "mode";
            public static final String SYNC = "syncActive";
            public static final String HDMI = "hdmiActive";
            public static final String SOURCE = "hdmiSource";
            public static final String BRIGHTNESS = "brightness";
        }
    }

    public static class CHANNELS {
        public static class DEVICE {
            public static class INFORMATION {
                public static final String FIRMWARE = "device-firmware#firmware";
                public static final String FIRMWARE_AVAILABLE = "device-firmware#available-firmware";
            }
        }

        public static class COMMANDS {
            public static final String MODE = "device-commands#mode";
            public static final String SYNC = "device-commands#sync-active";
            public static final String HDMI = "device-commands#hdmi-active";
            public static final String SOURCE = "device-commands#hdmi-source";
            public static final String BRIGHTNESS = "device-commands#brightness";
        }

        public static class HDMI {
            public static final HdmiChannels IN_1 = new HdmiChannels("device-hdmi-in-1#name", "device-hdmi-in-1#type",
                    "device-hdmi-in-1#mode", "device-hdmi-in-1#status");
            public static final HdmiChannels IN_2 = new HdmiChannels("device-hdmi-in-2#name", "device-hdmi-in-2#type",
                    "device-hdmi-in-2#mode", "device-hdmi-in-2#status");
            public static final HdmiChannels IN_3 = new HdmiChannels("device-hdmi-in-3#name", "device-hdmi-in-3#type",
                    "device-hdmi-in-3#mode", "device-hdmi-in-3#status");
            public static final HdmiChannels IN_4 = new HdmiChannels("device-hdmi-in-4#name", "device-hdmi-in-4#type",
                    "device-hdmi-in-4#mode", "device-hdmi-in-4#status");

            public static final HdmiChannels OUT = new HdmiChannels("device-hdmi-out#name", "device-hdmi-out#type",
                    "device-hdmi-out#mode", "device-hdmi-out#status");
        }
    }

    public static final String APPLICATION_NAME = "openHAB";

    /** Minimal API Version required. Only apiLevel >= 7 is supported. */
    public static final Integer MINIMAL_API_VERSION = 7;

    public static final String BINDING_ID = "huesync";
    public static final String THING_TYPE_ID = "box";
    public static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDING_ID, THING_TYPE_ID);

    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_PORT = "port";

    public static final String REGISTRATION_ID = "registrationId";
    public static final String API_TOKEN = "apiAccessToken";
}
