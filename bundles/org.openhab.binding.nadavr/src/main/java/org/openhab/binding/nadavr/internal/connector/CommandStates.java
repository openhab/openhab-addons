/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nadavr.internal.connector;

import java.util.HashMap;

/**
 * The {@link CommandStates.java} class contains fields mapping thing configuration parameters.
 *
 * @author Dave J Schoepel - Initial contribution
 */

public class CommandStates {

    private static HashMap<String, String> avrCommandStates = new HashMap<String, String>();

    /**
     *
     */
    // public CommandStates() {
    // avrCommandStates = new HashMap<String, String>();
    // }

    public void setCommandState(String command, String value) {
        avrCommandStates.put(command, value);
    }

    public String getCommandState(String command) {
        return avrCommandStates.get(command);
    }

}
