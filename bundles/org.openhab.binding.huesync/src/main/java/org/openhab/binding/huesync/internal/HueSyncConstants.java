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
    }

    public static class CHANNELS {
        public static class DEVICE {
            public static class INFORMATION {
                public static final String FIRMWARE = "device-firmware#firmware";
                public static final String FIRMWARE_AVAILABLE = "device-firmware#available-firmware";
            }
        }

        public static class HDMI {
            public static class IN_1 {
                public static final String NAME = "device-hdmi-in-1#name";
                public static final String TYPE = "device-hdmi-in-1#type";
            }

            public static class IN_2 {
                public static final String NAME = "device-hdmi-in-2#name";
                public static final String TYPE = "device-hdmi-in-2#type";
            }

            public static class IN_3 {
                public static final String NAME = "device-hdmi-in-3#name";
                public static final String TYPE = "device-hdmi-in-3#type";
            }

            public static class IN_4 {
                public static final String NAME = "device-hdmi-in-4#name";
                public static final String TYPE = "device-hdmi-in-4#type";
            }

            public static class OUT {
                public static final String NAME = "device-hdmi-out#name";
                public static final String TYPE = "device-hdmi-out#type";
            }
        }
    }

    public static final String APPLICATION_NAME = "openHAB";

    /** Minimal API Version required. Only apiLevel >= 7 is supported. */
    public static final Integer MINIMAL_API_VERSION = 7;

    public static final String BINDING_ID = "huesync";
    public static final String THING_TYPE_ID = "huesyncthing";
    public static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDING_ID, THING_TYPE_ID);

    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_PORT = "port";

    public static final Integer REGISTRATION_INITIAL_DELAY = 3;
    public static final Integer REGISTRATION_INTERVAL = 1;

    public static final String REGISTRATION_ID = "registrationId";
    public static final String API_TOKEN = "apiAccessToken";
}
