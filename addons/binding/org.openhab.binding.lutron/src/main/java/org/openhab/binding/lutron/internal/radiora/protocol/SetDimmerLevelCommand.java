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

/**
 * Set Dimmer Level (SDL)
 * Set an individual Dimmer’s light level.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class SetDimmerLevelCommand extends RadioRACommand {

    private int zoneNumber; // 1 to 32
    private int dimmerLevel; // 0 to 100
    private Integer fadeSec; // 0 to 240 (optional)

    public SetDimmerLevelCommand(int zoneNumber, int dimmerLevel) {
        this.zoneNumber = zoneNumber;
        this.dimmerLevel = dimmerLevel;
    }

    public void setFadeSeconds(int seconds) {
        fadeSec = seconds;
    }

    @Override
    public String getCommand() {
        return "SDL";
    }

    @Override
    public List<String> getArgs() {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(zoneNumber));
        args.add(String.valueOf(dimmerLevel));

        if (fadeSec != null) {
            args.add(String.valueOf(fadeSec));
        }

        return args;
    }

}
