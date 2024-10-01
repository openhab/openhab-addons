/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Button Press (BP) Command.
 * Trigger a Phantom Button Press on the RadioRA Serial Device.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class ButtonPressCommand extends RadioRACommand {

    public enum ButtonState {
        OFF,
        ON,
        TOG
    }

    private int buttonNumber; // 1 to 15, 16 ALL ON, 17 ALL OFF
    private ButtonState state; // ON/OFF/TOG
    private @Nullable Integer fadeSec; // 0 to 240 (optional)
    private int system; // 1 or 2, or 0 for none

    public ButtonPressCommand(int buttonNumber, ButtonState state, int system) {
        this.buttonNumber = buttonNumber;
        this.state = state;
        this.system = system;
    }

    public void setFadeSeconds(int seconds) {
        this.fadeSec = seconds;
    }

    @Override
    public String getCommand() {
        return "BP";
    }

    @Override
    public List<String> getArgs() {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(buttonNumber));
        args.add(String.valueOf(state));

        if (fadeSec != null) {
            args.add(String.valueOf(fadeSec));
        }

        if (system == 1 || system == 2) {
            args.add("S" + system);
        }

        return args;
    }
}
