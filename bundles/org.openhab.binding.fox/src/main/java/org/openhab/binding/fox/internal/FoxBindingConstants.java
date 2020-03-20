/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FoxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class FoxBindingConstants {

    private static final String BINDING_ID = "fox";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SYSTEM = new ThingTypeUID(BINDING_ID, "system");

    // List of all Channel ids
    public static final String CHANNEL_TASK_COMMAND = "taskCommand";
    public static final String CHANNEL_TASK_STATE = "taskState";
    public static final String CHANNEL_RESULT_TRIGGER = "resultTrigger";
    public static final String CHANNEL_RESULT_STATE = "resultState";
}
