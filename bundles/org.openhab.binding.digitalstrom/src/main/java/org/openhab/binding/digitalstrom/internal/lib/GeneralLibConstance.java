/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib;

/**
 * The {@link GeneralLibConstance} contains all relevant library constants.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class GeneralLibConstance {

    /**
     * digitalSTROMs broadcast zone id.
     */
    public static final int BROADCAST_ZONE_GROUP_ID = 0;

    /**
     * digitalSTROMs broadcast zone string by query response.
     */
    public static final String QUERY_BROADCAST_ZONE_STRING = "zone0";

    /**
     * Scene-Array index for the scene value.
     */
    public static final int SCENE_ARRAY_INDEX_VALUE = 0;
    /**
     * Scene-Array index for the scene value.
     */
    public static final int SCENE_ARRAY_INDEX_ANGLE = 1;

    /**
     * The highest read out priority for
     * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob}s.
     */
    public static final int HIGHEST_READ_OUT_PRIORITY = 0;
}
