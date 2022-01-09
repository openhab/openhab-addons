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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NikoHomeControlConstants} class defines common constants used in the Niko Home Control communication.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlConstants {

    // Action types abstracted from NhcI and NhcII action types
    public static enum ActionType {
        TRIGGER,
        RELAY,
        DIMMER,
        ROLLERSHUTTER,
        GENERIC
    }

    // switch and dimmer constants in the Nhc layer
    public static final String NHCON = "On";
    public static final String NHCOFF = "Off";

    public static final String NHCTRUE = "True";
    public static final String NHCFALSE = "False";

    public static final String NHCTRIGGERED = "Triggered";

    // rollershutter constants in the Nhc layer
    public static final String NHCDOWN = "Down";
    public static final String NHCUP = "Up";
    public static final String NHCSTOP = "Stop";

    // NhcII thermostat modes
    public static final String[] THERMOSTATMODES = { "Day", "Night", "Eco", "Off", "Cool", "Prog1", "Prog2", "Prog3" };
}
