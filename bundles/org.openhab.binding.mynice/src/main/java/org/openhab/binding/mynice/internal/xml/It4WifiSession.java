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
package org.openhab.binding.mynice.internal.xml;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link It4WifiSession} is responsible for handling datas of the current session
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class It4WifiSession {
    private final static Bundle BUNDLE = FrameworkUtil.getBundle(It4WifiSession.class);
    private final static String APP_ID = BUNDLE.getSymbolicName();

    private final Logger logger = LoggerFactory.getLogger(It4WifiSession.class);
    private final String clientChallenge = UUID.randomUUID().toString().substring(0, 8);
    private final byte[] clientChallengeArr = Utils.invertArray(DatatypeConverter.parseHexBinary(clientChallenge));

    private int sessionID = 1;
    private int commandSequence = 0;
    private byte[] sessionPassword = {};

    public int getCommandId() {
        return (commandSequence++ << 8) | (sessionID & 255);
    }

    public byte[] getSessionPassword() {
        return sessionPassword;
    }

    public String getClientChallenge() {
        return clientChallenge;
    }

    public String getUserName() {
        return APP_ID;
    }

    public void setChallenges(String serverChallenge, int sessionId, String password) {
        byte[] serverChallengeArr = Utils.invertArray(DatatypeConverter.parseHexBinary(serverChallenge));
        byte[] pairingPassword = Base64.getDecoder().decode(password);
        this.sessionID = sessionId;
        try {
            sessionPassword = Utils.sha256(pairingPassword, serverChallengeArr, clientChallengeArr);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Error generating session password : {}", e.getMessage());
        }
    }
}
