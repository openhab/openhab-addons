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
package org.openhab.binding.boschshc.internal.services.silentmode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

/**
 * Enum for possible silent mode states.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public enum SilentModeState {
    MODE_NORMAL,
    MODE_SILENT;

    public static SilentModeState fromOnOffType(OnOffType onOffCommand) {
        return onOffCommand == OnOffType.ON ? SilentModeState.MODE_SILENT : SilentModeState.MODE_NORMAL;
    }
}
