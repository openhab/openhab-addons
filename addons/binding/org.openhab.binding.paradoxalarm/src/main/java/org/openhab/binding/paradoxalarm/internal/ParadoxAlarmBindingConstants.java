/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal;

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

    private static final String binding_id = "paradoxalarm";

    private static final String communicator_id = "paradoxCommunication";
    private static final String partition_id = "paradoxPartition";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_COMMUNICATOR = new ThingTypeUID(communicator_id, binding_id);
    public static final ThingTypeUID THING_TYPE_PARTITION = new ThingTypeUID(partition_id, binding_id);

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";
}
