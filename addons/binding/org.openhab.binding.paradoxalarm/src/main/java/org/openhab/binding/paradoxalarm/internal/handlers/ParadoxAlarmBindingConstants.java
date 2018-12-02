/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.handlers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ParadoxAlarmBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
public class ParadoxAlarmBindingConstants {

    private static final String BINDING_ID = "paradoxalarm";

    private static final String PARADOX_COMMUNICATOR_THING_TYPE_ID = "ip150";

    private static final String PARADOX_PANEL_THING_TYPE_ID = "panel";

    private static final String PARTITION_THING_TYPE_ID = "partition";

    private static final String ZONE_THING_TYPE_ID = "zone";

    // List of all Thing Type UIDs
    public static final ThingTypeUID COMMUNICATOR_THING_TYPE_UID = new ThingTypeUID(BINDING_ID,
            PARADOX_COMMUNICATOR_THING_TYPE_ID);
    public static final ThingTypeUID PANEL_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, PARADOX_PANEL_THING_TYPE_ID);
    public static final ThingTypeUID PARTITION_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, PARTITION_THING_TYPE_ID);
    public static final ThingTypeUID ZONE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, ZONE_THING_TYPE_ID);
    public static final String IP150_COMMUNICATION_COMMAND_CHANNEL_UID = "communicationCommand";

    // List of all Channel ids
    public static final String STATE = "state";

}
