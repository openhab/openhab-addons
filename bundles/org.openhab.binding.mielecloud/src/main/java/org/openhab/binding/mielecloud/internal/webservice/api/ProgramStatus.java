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
package org.openhab.binding.mielecloud.internal.webservice.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the status of a program.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public enum ProgramStatus {
    PROGRAM_STARTED("start"),
    PROGRAM_STOPPED("stop"),
    PROGRAM_PAUSED("pause");

    /**
     * Corresponding state of the ChannelTypeDefinition
     */
    private String state;

    ProgramStatus(String value) {
        this.state = value;
    }

    /**
     * Checks whether the given value is the raw state represented by this enum instance.
     */
    public boolean matches(String passedValue) {
        return state.equalsIgnoreCase(passedValue);
    }

    /**
     * Gets the raw state.
     */
    public String getState() {
        return state;
    }
}
