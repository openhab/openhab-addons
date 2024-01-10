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
import org.openhab.core.library.types.OnOffType;

/**
 * Set Switch Level (SSL)
 * Turn an individual Switch ON or OFF.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class SetSwitchLevelCommand extends RadioRACommand {

    private int zoneNumber; // 1 to 32
    private OnOffType state; // ON/OFF
    private @Nullable Integer delaySec; // 0 to 240 (optional)
    private int system; // 1 or 2, or 0 for none

    public SetSwitchLevelCommand(int zoneNumber, OnOffType state, int system) {
        this.zoneNumber = zoneNumber;
        this.state = state;
        this.system = system;
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

        if (system == 1 || system == 2) {
            args.add("S" + system);
        }

        return args;
    }
}
