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
package org.openhab.binding.vallox.internal.se.mapper;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;

/**
 * Class for string channels.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class StringChannel extends ValloxChannel {

    /**
     * Create new instance.
     *
     * @param variable channel as byte
     */
    public StringChannel(byte variable) {
        super(variable);
    }

    @Override
    public State convertToState(byte value) {
        String noneInstalled = "None";
        StringBuilder sb = new StringBuilder();

        if ((value & 0x02) != 0) {
            sb.append("1");
        }
        if ((value & 0x04) != 0) {
            sb.append(",2");
        }
        if ((value & 0x08) != 0) {
            sb.append(",3");
        }
        if ((value & 0x10) != 0) {
            sb.append(",4");
        }
        if ((value & 0x20) != 0) {
            sb.append(",5");
        }

        if (sb.toString().equals("")) {
            return new StringType(noneInstalled);
        }
        return new StringType(sb.toString());
    }
}
