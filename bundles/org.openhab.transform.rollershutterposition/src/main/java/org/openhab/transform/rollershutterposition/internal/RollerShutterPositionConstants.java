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
package org.openhab.transform.rollershutterposition.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;

/**
 * The {@link RollerShutterPositionConstants} class to define transform constants
 * used across the whole binding.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class RollerShutterPositionConstants {

    // Profile Type UID
    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "ROLLERSHUTTERPOSITION");

    // Parameters
    public static final String UPTIME_PARAM = "uptime";
    public static final String DOWNTIME_PARAM = "downtime";
    public static final String PRECISION_PARAM = "precision";

    public static final int POSITION_UPDATE_PERIOD_MILLISECONDS = 800;
    public static final int DEFAULT_PRECISION = 5;
}
