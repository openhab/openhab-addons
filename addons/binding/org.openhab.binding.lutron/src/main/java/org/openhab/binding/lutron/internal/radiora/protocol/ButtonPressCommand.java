/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Button Press (BP) Command.
 * Trigger a Phantom Button Press on the RadioRA Serial Device.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class ButtonPressCommand extends RadioRACommand {

    public enum ButtonState {
        OFF,
        ON,
        TOG;
    }

    private int buttonNumber; // 1 to 15, 16 ALL ON, 17 ALL OFF
    private ButtonState state; // ON/OFF/TOG
    private Integer fadeSec; // 0 to 240 (optional)

    public ButtonPressCommand(int buttonNumber, ButtonState state) {
        this.buttonNumber = buttonNumber;
        this.state = state;
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

        return args;
    }

}
