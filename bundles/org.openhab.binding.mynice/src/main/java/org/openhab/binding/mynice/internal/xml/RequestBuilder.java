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
package org.openhab.binding.mynice.internal.xml;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.T4Command;

/**
 * The {@link RequestBuilder} is responsible for building a string request from the CommandType
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RequestBuilder {
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();

    public static final String USERNAME = "%un%";
    public static final String CLIENT_CHALLENGE = "%cc%";
    private static final String START_REQUEST = "<Request id=\"%s\" source=\"openhab\" target=\"%s\" gw=\"gwID\" protocolType=\"NHK\" protocolVersion=\"1.0\" type=\"%s\">\r\n";
    private static final String END_REQUEST = "%s%s</Request>";
    private static final String DOOR_ACTION = "<DoorAction>%s</DoorAction>";
    private static final String T4_ACTION = "<T4Action>%s</T4Action>";
    private static final String SIGN = "<Sign>%s</Sign>";

    private final String clientChallenge = UUID.randomUUID().toString().substring(0, 8);
    private final byte[] clientChallengeArr = invertArray(DatatypeConverter.parseHexBinary(clientChallenge));
    private final MessageDigest digest;
    private final String it4WifiMac;
    private final String username;

    private int sessionId = 0;
    private int commandSequence = 0;
    private byte[] sessionPassword = {};

    public RequestBuilder(String it4WifiMac, String username) {
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
            this.it4WifiMac = it4WifiMac;
            this.username = username;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String buildSign(CommandType command, String message) {
        if (command.signNeeded) {
            byte[] msgHash = sha256(message.getBytes());
            byte[] sign = sha256(msgHash, sessionPassword);
            return String.format(SIGN, BASE64_ENCODER.encodeToString(sign));
        }
        return "";
    }

    public String buildMessage(String id, String command) {
        return buildMessage(CommandType.CHANGE, id, String.format(DOOR_ACTION, command.toLowerCase()));
    }

    public String buildMessage(String id, T4Command t4) {
        return buildMessage(CommandType.CHANGE, id, String.format(T4_ACTION, t4.name()));
    }

    public String buildMessage(CommandType command, Object... bodyParms) {
        String startRequest = String.format(START_REQUEST, getCommandId(), it4WifiMac, command);
        String body = startRequest + getBody(command, bodyParms);
        String sign = buildSign(command, body);
        return String.format(END_REQUEST, body, sign);
    }

    public String getBody(CommandType command, Object... bodyParms) {
        String result = command.body;
        if (result.length() != 0) {
            result = result.replace(USERNAME, username);
            result = result.replace(CLIENT_CHALLENGE, clientChallenge);
            result = String.format(result, bodyParms);
        }
        return result;
    }

    public int getCommandId() {
        return (commandSequence++ << 8) | sessionId;
    }

    public void setChallenges(String serverChallenge, int sessionId, String password) {
        byte[] serverChallengeArr = invertArray(DatatypeConverter.parseHexBinary(serverChallenge));
        byte[] pairingPassword = Base64.getDecoder().decode(password);
        this.sessionPassword = sha256(pairingPassword, serverChallengeArr, clientChallengeArr);
        this.sessionId = sessionId & 255;
    }

    private byte[] sha256(byte[]... values) {
        for (byte[] data : values) {
            digest.update(data);
        }
        return digest.digest();
    }

    private static byte[] invertArray(byte[] data) {
        byte[] result = new byte[data.length];
        int i = data.length - 1;
        int c = 0;
        while (i >= 0) {
            int c2 = c + 1;
            result[c] = data[i];
            i--;
            c = c2;
        }
        return result;
    }
}
