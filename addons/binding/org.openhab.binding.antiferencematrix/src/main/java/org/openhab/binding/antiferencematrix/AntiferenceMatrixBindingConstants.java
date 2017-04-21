/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.antiferencematrix;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AntiferenceMatrixBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Neil Renaud - Initial contribution
 */
public class AntiferenceMatrixBindingConstants {

    public static final String BINDING_ID = "antiferencematrix";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_MATRIX_OUTPUT = new ThingTypeUID(BINDING_ID, "matrixoutput");
    public final static ThingTypeUID THING_TYPE_MATRIX_INPUT = new ThingTypeUID(BINDING_ID, "matrixinput");
    public final static ThingTypeUID THING_TYPE_MATRIX = new ThingTypeUID(BINDING_ID, "matrix");

    // List of all Channel ids
    public final static String SOURCE_CHANNEL = "source";
    public final static String POWER_CHANNEL = "power";
    public static final String MATRIX_STATUS_MESSAGE_CHANNEL = "matrixstatusmessage";
    public static final String PORT_STATUS_MESSAGE_CHANNEL = "statusmessage";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_MATRIX_OUTPUT,
            THING_TYPE_MATRIX_INPUT, THING_TYPE_MATRIX);

    public static final String PROPERTY_OUTPUT_ID = "outputId";
    public static final String PROPERTY_INPUT_ID = "inputId";

}
