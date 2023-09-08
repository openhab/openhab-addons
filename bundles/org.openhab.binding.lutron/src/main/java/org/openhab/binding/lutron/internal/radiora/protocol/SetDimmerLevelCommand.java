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
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Set Dimmer Level (SDL)
 * Set an individual Dimmer’s light level.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class SetDimmerLevelCommand extends RadioRACommand {

    private int zoneNumber; // 1 to 32
    private int dimmerLevel; // 0 to 100
    private @Nullable Integer fadeSec; // 0 to 240 (optional)
    private int system; // 1 or 2, or 0 for none

    public SetDimmerLevelCommand(int zoneNumber, int dimmerLevel, int system) {
        this.zoneNumber = zoneNumber;
        this.dimmerLevel = dimmerLevel;
        this.system = system;
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

        if (system == 1 || system == 2) {
            args.add("S" + system);
        }

        return args;
    }
}
