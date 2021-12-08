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
package org.openhab.binding.luxom.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class CommandExecutionSpecification {
    private final String command;
    private final boolean addExtraSendPermit; // some commands do not receive an ACK -> therefore we must add extra
                                              // send permits

    public CommandExecutionSpecification(String command, boolean addExtraSendPermit) {
        this.command = command;
        this.addExtraSendPermit = addExtraSendPermit;
    }

    public String getCommand() {
        return command;
    }

    public boolean isAddExtraSendPermit() {
        return addExtraSendPermit;
    }
}
