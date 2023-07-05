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
package org.openhab.binding.toyota.internal.dto;

import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class describes the current status of a car window
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Window extends ThingStatus {
    public enum WindowClosingState {
        @SerializedName("close")
        CLOSED(OpenClosedType.CLOSED),
        @SerializedName("open")
        OPENED(OpenClosedType.OPEN),
        UNKNOWN(UnDefType.NULL);

        public final State state;

        WindowClosingState(State state) {
            this.state = state;
        }
    }

    public WindowClosingState state;
}
