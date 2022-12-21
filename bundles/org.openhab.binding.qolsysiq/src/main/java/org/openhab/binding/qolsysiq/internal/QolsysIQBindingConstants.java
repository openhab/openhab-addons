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
package org.openhab.binding.qolsysiq.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link QolsysIQBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class QolsysIQBindingConstants {

    public static final String BINDING_ID = "qolsysiq";

    public static final ThingTypeUID THING_TYPE_PANEL = new ThingTypeUID(BINDING_ID, "panel");
    public static final ThingTypeUID THING_TYPE_PARTITION = new ThingTypeUID(BINDING_ID, "partition");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");

    public static final String CHANNEL_PARTITION_ARM_STATE = "armState";
    public static final String CHANNEL_PARTITION_ALARM_STATE = "alarmState";
    public static final String CHANNEL_PARTITION_COMMAND_DELAY = "armingDelay";
    public static final String CHANNEL_PARTITION_ERROR_EVENT = "errorEvent";

    public static final String CHANNEL_ZONE_STATE = "state";
    public static final String CHANNEL_ZONE_STATUS = "status";
    public static final String CHANNEL_ZONE_CONTACT = "contact";
}
