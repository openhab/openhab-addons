/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * Set Switch Level (SSL)
 * Turn an individual Switch ON or OFF.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class SetSwitchLevelCommand extends RadioRACommand {

    private int zoneNumber; // 1 to 32
    private OnOffType state; // ON/OFF
    private Integer delaySec; // 0 to 240 (optional)

    public SetSwitchLevelCommand(int zoneNumber, OnOffType state) {
        this.zoneNumber = zoneNumber;
        this.state = state;
    }

    public void setDelaySeconds(int seconds) {
        this.delaySec = seconds;
    }

    @Override
    public String getCommand() {
        return "SSL";
    }

    @Override
    public List<String> getArgs() {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(zoneNumber));
        args.add(String.valueOf(state));

        if (delaySec != null) {
            args.add(String.valueOf(delaySec));
        }

        return args;
    }

}
