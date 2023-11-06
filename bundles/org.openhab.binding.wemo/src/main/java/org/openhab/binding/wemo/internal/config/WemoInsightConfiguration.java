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
package org.openhab.binding.wemo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for a WeMo Insight Switch
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoInsightConfiguration {

    public static final String CURRENT_POWER_SLIDING_SECONDS = "currentPowerSlidingSeconds";
    public static final String CURRENT_POWER_DELTA_TRIGGER = "currentPowerDeltaTrigger";

    @Nullable
    public String udn;
    public int currentPowerSlidingSeconds = 60;
    public int currentPowerDeltaTrigger = 1;
}
