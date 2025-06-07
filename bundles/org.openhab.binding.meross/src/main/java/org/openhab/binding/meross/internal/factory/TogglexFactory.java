/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.meross.internal.command.Command;
import org.openhab.binding.meross.internal.command.TogglexCommand;

/**
 * The {@link TypeFactory} class is responsible for switching among different togglex modes
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class TogglexFactory extends ModeFactory {
    @Override
    public Command commandMode(String mode) {
        return switch (mode) {
            case "ON" -> new TogglexCommand.TurnOn();
            case "OFF" -> new TogglexCommand.TurnOff();
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }
}
