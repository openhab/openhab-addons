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
import org.openhab.binding.meross.internal.command.DoorStateCommand;
import org.openhab.binding.meross.internal.command.MerossCommand;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TypeFactory} class is responsible for converting to garage door state modes
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class DoorStateFactory extends ModeFactory {
    @Override
    public MerossCommand commandMode(Command command, @Nullable Integer deviceChannel) {
        int channel = deviceChannel != null ? deviceChannel : 0;
        return switch (command) {
            case UpDownType.UP -> new DoorStateCommand.Up(channel);
            case UpDownType.DOWN -> new DoorStateCommand.Down(channel);
            default -> throw new IllegalStateException("Unexpected value: " + command.toString());
        };
    }

    @Override
    public State state(int merossState) {
        return switch (merossState) {
            case 0 -> OpenClosedType.OPEN;
            case 1 -> OpenClosedType.CLOSED;
            default -> UnDefType.UNDEF;
        };
    }
}
