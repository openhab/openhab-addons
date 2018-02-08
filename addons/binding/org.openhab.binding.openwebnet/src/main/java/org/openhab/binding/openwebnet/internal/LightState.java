/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Provide the status of the light on update
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public class LightState {

    public static final int REFRESH = -2;
    public static final int ERROR = -1;

    public final String channel;
    public final State state;

    LightState(String channel, int value) {
        this.channel = channel;
        this.state = fromOpenWebNet(value);
    }

    /**
     * Convert from OpenWebNet state to SmartHome state
     *
     * @param state OpenWebNet light state
     * @return SmartHome State
     */
    private static State fromOpenWebNet(int state) {
        switch (state) {
            case 0:
                return OnOffType.OFF;
            case 1:
                return OnOffType.ON;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                return new PercentType(state * 10);
            default:
                // Unknown command
                return UnDefType.UNDEF;
        }
    }

    /**
     * Convert from SmartHome state to OpenWebNet light state
     *
     * @param state
     * @return OpenWebNet state or ERROR
     */
    public static int toOpenWebNet(State state) {
        if (state instanceof OnOffType) {
            switch ((OnOffType) state) {
                case ON:
                    return 1;
                case OFF:
                    return 0;
            }
        } else {
            if (state instanceof PercentType) {
                int percent = ((PercentType) state).intValue();
                if (percent == 0) {
                    return 0;
                }
                int out = (percent / 10);
                if ((percent % 10) > 0) {
                    out++;
                }
                return out;
            }
        }
        return ERROR;
    }

    /**
     * Convert from SmartHome command to OpenWebNet light state
     *
     * @param command
     * @return OpenWebNet state or REFRESH or ERROR
     */
    public static int toOpenWebNet(Command command) {
        if (command instanceof OnOffType) {
            switch ((OnOffType) command) {
                case ON:
                    return 1;
                case OFF:
                    return 0;
            }
        } else if (command instanceof PercentType) {
            int percent = ((PercentType) command).intValue();
            if (percent == 0) {
                return 0;
            }
            int out = (percent / 10);
            if ((percent % 10) > 0) {
                out++;
            }
            if (out == 1) {
                // 10% is not possible to be set, so put 20%
                out++;
            }
            return out;
        } else if (RefreshType.REFRESH.equals(command)) {
            return REFRESH;
        }
        return ERROR;
    }

}
