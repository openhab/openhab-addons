/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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