/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.command.MerossCommand;
import org.openhab.binding.meross.internal.command.TogglexCommand;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TypeFactory} class is responsible for switching among different togglex modes
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Add state update
 */
@NonNullByDefault
public class TogglexFactory extends ModeFactory {
    @Override
    public MerossCommand commandMode(Command command, @Nullable Integer deviceChannel) {
        int channel = deviceChannel != null ? deviceChannel : 0;
        return switch (command) {
            case OnOffType.ON -> new TogglexCommand.TurnOn(channel);
            case OnOffType.OFF -> new TogglexCommand.TurnOff(channel);
            default -> throw new IllegalStateException("Unexpected value: " + command.toString());
        };
    }

    @Override
    public State state(int merossState) {
        return switch (merossState) {
            case 0 -> OnOffType.OFF;
            case 1 -> OnOffType.ON;
            default -> UnDefType.UNDEF;
        };
    }
}
