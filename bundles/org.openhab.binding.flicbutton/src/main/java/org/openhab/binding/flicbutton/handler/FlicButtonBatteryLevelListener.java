/**
 * Copyright (c) 2016 - 2020 Patrick Fink
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3
 * with the GNU Classpath Exception 2.0 which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0 WITH Classpath-exception-2.0
 */
package org.openhab.binding.flicbutton.handler;

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
