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
package org.openhab.binding.mynice.internal.xml.dto;

import static org.openhab.binding.mynice.internal.xml.RequestBuilder.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The CommandType enum lists all handled command with according syntax
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum CommandType {
    PAIR(false,
            "<Authentication username=\"" + USERNAME
                    + "\" cc=\"null\" CType=\"phone\" OSType=\"Android\" OSVer=\"6.0.1\"/>"),
    VERIFY(false, "<User username=\"" + USERNAME + "\"/>"),
    CONNECT(false, "<Authentication username=\"" + USERNAME + "\" cc=\"" + CLIENT_CHALLENGE + "\"/>"),
    INFO(true, ""),
    STATUS(true, ""),
    CHANGE(true, "<Devices><Device id=\"%s\"><Services>%s</Services></Device></Devices>");

    public final boolean signNeeded;
    public final String body;

    CommandType(boolean signNeeded, String body) {
        this.signNeeded = signNeeded;
        this.body = body;
    }
}
