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
package org.openhab.binding.sensorpush.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * SensorPush REST API endpoint definitions
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class Endpoint {
    public static final String AUTHORIZE = "/api/v1/oauth/authorize";
    public static final String ACCESSTOKEN = "/api/v1/oauth/accesstoken";
    public static final String SENSORS = "/api/v1/devices/sensors";
    public static final String GATEWAYS = "/api/v1/devices/gateways";
    public static final String SAMPLES = "/api/v1/samples";
}
