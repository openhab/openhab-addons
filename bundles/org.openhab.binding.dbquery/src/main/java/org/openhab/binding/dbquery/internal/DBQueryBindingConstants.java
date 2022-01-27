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
package org.openhab.binding.dbquery.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Common constants, which are used across the whole binding.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class DBQueryBindingConstants {

    private static final String BINDING_ID = "dbquery";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INFLUXDB2_BRIDGE = new ThingTypeUID(BINDING_ID, "influxdb2");
    public static final ThingTypeUID THING_TYPE_QUERY = new ThingTypeUID(BINDING_ID, "query");

    // List of all Channel ids
    public static final String CHANNEL_EXECUTE = "execute";

    public static final String CHANNEL_PARAMETERS = "parameters";
    public static final String CHANNEL_CORRECT = "correct";
    public static final String TRIGGER_CHANNEL_CALCULATE_PARAMETERS = "calculateParameters";

    public static final String RESULT_STRING_CHANNEL_TYPE = "result-channel-string";
    public static final String RESULT_NUMBER_CHANNEL_TYPE = "result-channel-number";
    public static final String RESULT_DATETIME_CHANNEL_TYPE = "result-channel-datetime";
    public static final String RESULT_CONTACT_CHANNEL_TYPE = "result-channel-contact";
    public static final String RESULT_SWITCH_CHANNEL_TYPE = "result-channel-switch";

    private DBQueryBindingConstants() {
    }
}
