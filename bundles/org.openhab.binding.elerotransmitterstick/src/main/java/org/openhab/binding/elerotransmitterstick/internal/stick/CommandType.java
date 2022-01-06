/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.elerotransmitterstick.internal.stick;

/**
 * The {@link CommandType} is the type of the {@link Command}.
 *
 * @author Volker Bier - Initial contribution
 */
public enum CommandType {
    UP,
    INTERMEDIATE,
    VENTILATION,
    DOWN,
    STOP,
    INFO,
    CHECK,
    NONE;

    public static CommandType getForPercent(int percentage) {
        if (percentage == 0) {
            return UP;
        }

        if (percentage == 25) {
            return CommandType.INTERMEDIATE;
        }

        if (percentage == 75) {
            return CommandType.VENTILATION;
        }

        if (percentage == 100) {
            return CommandType.DOWN;
        }

        return null;
    }
}
