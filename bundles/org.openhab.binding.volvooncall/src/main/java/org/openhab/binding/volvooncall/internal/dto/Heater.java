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
package org.openhab.binding.volvooncall.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link Heater} is responsible for storing
 * heater information returned by vehicle status rest answer
 *
 * @author Arie van der Lee - Initial contribution
 */
@NonNullByDefault
public class Heater {
    private String status = "";

    /*
     * Currently unused in the binding, maybe interesting in the future
     * private ZonedDateTime timestamp;
     */
    public State getStatus() {
        if ("off".equalsIgnoreCase(status)) {
            return OnOffType.OFF;
        } else if ("on".equalsIgnoreCase(status) || "onOther".equalsIgnoreCase(status)) {
            return OnOffType.ON;
        }
        return UnDefType.UNDEF;
    }
}
