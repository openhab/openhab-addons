/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link GmailParadoxParserBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
public class GmailParadoxParserBindingConstants {

    private static final String BINDING_ID = "gmailparadoxparser";

    // List of all Thing Type UIDs
    public static final ThingTypeUID PANEL_COMMUNICATION_THING_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "paradoxCommunication");
    public static final ThingTypeUID PARTITION_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "paradoxPartition");

    // List of all Channel ids
    public static final String STATE = "state";

    public static Map<String, String> partitionIdMap = new HashMap<>();
}
