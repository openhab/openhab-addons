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
package org.openhab.binding.mynice.internal.xml.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.xml.It4WifiSession;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum CommandType {
    PAIR(false, "Authentication username=\"%un%\" cc=\"null\" CType=\"phone\" OSType=\"Android\" OSVer=\"6.0.1\""),
    VERIFY(false, "User username=\"%un%\""),
    CONNECT(false, "Authentication username=\"%un%\" cc=\"%cc%\""),
    INFO,
    STATUS,
    CHANGE;

    public final boolean signNeeded;
    private final String body;

    CommandType(boolean signNeeded, String body) {
        this.signNeeded = signNeeded;
        this.body = body;
    }

    CommandType() {
        this(true, "");
    }

    public String getBody(It4WifiSession session/* , Object... bodyParms */) {
        if (body.length() == 0) {
            return body;
        }
        String result = body.replace("%un%", session.getUserName());
        result = result.replace("%cc%", session.getClientChallenge());
        return String.format("<%s/>", result/* , bodyParms */);
    }
}
