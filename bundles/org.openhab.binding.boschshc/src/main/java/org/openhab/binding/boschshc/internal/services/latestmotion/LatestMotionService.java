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
package org.openhab.binding.boschshc.internal.services.latestmotion;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.latestmotion.dto.LatestMotionServiceState;

/**
 * Detects every movement through an intelligent combination of passive infra-red technology and an additional
 * temperature sensor.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class LatestMotionService extends BoschSHCService<LatestMotionServiceState> {

    public LatestMotionService() {
        super("LatestMotion", LatestMotionServiceState.class);
    }
}
