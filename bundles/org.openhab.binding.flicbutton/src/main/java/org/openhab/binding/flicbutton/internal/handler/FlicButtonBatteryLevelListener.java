/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;

import io.flic.fliclib.javaclient.BatteryStatusListener;
import io.flic.fliclib.javaclient.Bdaddr;

/**
 *
 * @author Patrick Fink
 *
 */
public class FlicButtonBatteryLevelListener extends BatteryStatusListener.Callbacks {

    private final FlicButtonHandler thingHandler;

    FlicButtonBatteryLevelListener(@NonNull FlicButtonHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    @Override
    public void onBatteryStatus(Bdaddr bdaddr, int i, long l) throws IOException {
        thingHandler.updateBatteryChannel(i);
    }
}
