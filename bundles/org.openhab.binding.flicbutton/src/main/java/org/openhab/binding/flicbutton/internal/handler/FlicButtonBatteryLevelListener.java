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
package org.openhab.binding.flicbutton.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.flic.fliclib.javaclient.BatteryStatusListener;
import io.flic.fliclib.javaclient.Bdaddr;

/**
 * Each {@link FlicButtonBatteryLevelListener} object listens to the battery status of a specific Flic button
 * and calls updates the {@link FlicButtonHandler} accordingly.
 *
 * @author Patrick Fink - Initial contribution
 *
 */
@NonNullByDefault
public class FlicButtonBatteryLevelListener extends BatteryStatusListener.Callbacks {

    private final FlicButtonHandler thingHandler;

    FlicButtonBatteryLevelListener(FlicButtonHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    @Override
    public void onBatteryStatus(@Nullable Bdaddr bdaddr, int i, long l) throws IOException {
        thingHandler.updateBatteryChannel(i);
    }
}
