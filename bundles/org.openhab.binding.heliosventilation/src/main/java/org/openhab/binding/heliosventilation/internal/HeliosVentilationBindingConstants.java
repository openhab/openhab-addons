/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heliosventilation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HeliosVentilationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentilationBindingConstants {

    public static final String BINDING_ID = "heliosventilation";

    public static final String DATAPOINT_FILE = "datapoints.properties";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HELIOS_VENTILATION = new ThingTypeUID(BINDING_ID, "ventilation");

    // List of all Channel ids
    // Channel ids are only in datapoints.properties and thing-types.xml
}
