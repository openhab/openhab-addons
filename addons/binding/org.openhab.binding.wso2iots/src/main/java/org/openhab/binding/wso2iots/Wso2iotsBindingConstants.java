/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wso2iots;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link Wso2iotsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ramesha Karunasena - Initial contribution
 */
public class Wso2iotsBindingConstants {

    public static final String BINDING_ID = "wso2iots";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "wso2iotsBridge");
    public static final ThingTypeUID THING_TYPE_BUILDINGMONITOR = new ThingTypeUID(BINDING_ID, "buildingMonitor");

    // List of all Channel ids
    public static final String LIGHT = "light";
    public static final String TEMPERATURE = "temperature";
    public static final String MOTION = "motion";
    public static final String HUMIDITY = "humidity";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BUILDINGMONITOR);
    public static final Set<String> SUPPORTED_CHANNEL_IDS = ImmutableSet.of(LIGHT, TEMPERATURE, MOTION, HUMIDITY);

}
