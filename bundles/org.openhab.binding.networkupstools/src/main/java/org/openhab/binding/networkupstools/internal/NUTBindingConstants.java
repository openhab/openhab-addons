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
package org.openhab.binding.networkupstools.internal;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link NUTBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class NUTBindingConstants {

    public static final String BINDING_ID = "networkupstools";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UPS = new ThingTypeUID(BINDING_ID, "ups");

    public static final String METADATA_NETWORKUPSTOOLS = "networkupstools";
    public static final ChannelTypeUID CHANNEL_TYPE_DYNAMIC_NUMBER = new ChannelTypeUID(BINDING_ID, "number");
    public static final ChannelTypeUID CHANNEL_TYPE_DYNAMIC_STRING = new ChannelTypeUID(BINDING_ID, "string");
    public static final ChannelTypeUID CHANNEL_TYPE_DYNAMIC_SWITCH = new ChannelTypeUID(BINDING_ID, "switch");
    public static final URI DYNAMIC_CHANNEL_CONFIG_QUANTITY_TYPE = URI
            .create("channel-type:ups:dynamic-channel-config-quantity-type");

    private static final String PARAMETER_PREFIX_UPS = "ups.";

    /**
     * Enum with nut names which value will be set a parameter on the thing.
     * These are values that don't change at all (e.g. type of ups) or not very often (e.g. firmware version).
     */
    public enum Parameters {
        UPS_FIRMWARE(PARAMETER_PREFIX_UPS + "firmware"),
        UPS_FIRMWARE_AUX(PARAMETER_PREFIX_UPS + "firmware.aux"),
        UPS_ID(PARAMETER_PREFIX_UPS + "id"),
        UPS_MFR(PARAMETER_PREFIX_UPS + "mfr"),
        UPS_MFR_DATE(PARAMETER_PREFIX_UPS + "mfr.date"),
        UPS_MODEL(PARAMETER_PREFIX_UPS + "model"),
        UPS_SERIAL(PARAMETER_PREFIX_UPS + "serial");

        private final String nutName;

        Parameters(final String nutName) {
            this.nutName = nutName;
        }

        public String getNutName() {
            return nutName;
        }
    }
}
