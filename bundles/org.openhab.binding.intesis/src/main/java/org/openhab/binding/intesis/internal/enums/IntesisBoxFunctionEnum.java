/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.intesis.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IntesisBoxFunctionEnum) contains informations for translating channels into internally used functions.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public enum IntesisBoxFunctionEnum {
    power("ONOFF"),
    targetTemperature("SETPTEMP"),
    mode("MODE"),
    fanSpeed("FANSP"),
    vanesUpDown("VANEUD"),
    vanesLeftRight("VANELR");

    private final String function;

    private IntesisBoxFunctionEnum(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }
}
