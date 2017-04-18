/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.types;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link OperationModeType} class is holding all OperationModes
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public enum OperationModeType implements PrimitiveType, State, Command {
    OFFLINE,
    STANDBY,
    INTERNET_RADIO,
    BLUETOOTH,
    AUX,
    AUX1,
    AUX2,
    AUX3,
    SPOTIFY,
    PANDORA,
    DEEZER,
    SIRIUSXM,
    STORED_MUSIC,
    AMAZON,
    TV,
    HDMI1,
    OTHER;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }

    @Override
    public String toFullString() {
        return super.toString();
    }
}