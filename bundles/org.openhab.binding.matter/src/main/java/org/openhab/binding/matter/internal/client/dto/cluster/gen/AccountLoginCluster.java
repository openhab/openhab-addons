/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * AccountLogin
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AccountLoginCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x050E;
    public static final String CLUSTER_NAME = "AccountLogin";
    public static final String CLUSTER_PREFIX = "accountLogin";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision

    // Structs
    /**
     * This event can be used by the Content App to indicate that the current user has logged out. In response to this
     * event, the Fabric Admin shall remove access to this Content App by the specified Node. If no Node is provided,
     * then the Fabric Admin shall remove access to all non-Admin Nodes.
     */
    public static class LoggedOut {
        /**
         * This field shall provide the Node ID corresponding to the user account that has logged out, if that Node ID
         * is available. If it is NOT available, this field shall NOT be present in the event.
         */
        public BigInteger node; // node-id

        public LoggedOut(BigInteger node) {
            this.node = node;
        }
    }

    public AccountLoginCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1294, "AccountLogin");
    }

    protected AccountLoginCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * The purpose of this command is to determine if the active user account of the given Content App matches the
     * active user account of a given Commissionee, and when it does, return a Setup PIN code which can be used for
     * password-authenticated session establishment (PASE) with the Commissionee.
     * For example, a Video Player with a Content App Platform may invoke this command on one of its Content App
     * endpoints to facilitate commissioning of a Phone App made by the same vendor as the Content App. If the accounts
     * match, then the Content App may return a setup code that can be used by the Video Player to commission the Phone
     * App without requiring the user to physically input a setup code.
     * The account match is determined by the Content App using a method which is outside the scope of this
     * specification and will typically involve a central service which is in communication with both the Content App
     * and the Commissionee. The GetSetupPIN command is needed in order to provide the Commissioner/Admin with a Setup
     * PIN when this Commissioner/Admin is operated by a different vendor from the Content App.
     * This method is used to facilitate Setup PIN exchange (for PASE) between Commissioner and Commissionee when the
     * same user account is active on both nodes. With this method, the Content App satisfies proof of possession
     * related to commissioning by requiring the same user account to be active on both Commissionee and Content App,
     * while the Commissioner/Admin ensures user consent by prompting the user prior to invocation of the command.
     * Upon receipt of this command, the Content App checks if the account associated with the Temporary Account
     * Identifier sent by the client is the same account that is active on itself. If the accounts are the same, then
     * the Content App returns the GetSetupPIN Response which includes a Setup PIN that may be used for PASE with the
     * Commissionee.
     * The Temporary Account Identifier for a Commissionee may be populated with the Rotating ID field of the client’s
     * commissionable node advertisement (see Rotating Device Identifier section in [MatterCore]) encoded as an octet
     * string where the octets of the Rotating Device Identifier are encoded as 2-character sequences by representing
     * each octet’s value as a 2-digit hexadecimal number, using uppercase letters.
     * The Setup PIN is a character string so that it can accommodate different future formats, including alpha-numeric
     * encodings. For a Commissionee it shall be populated with the Manual Pairing Code (see Manual Pairing Code section
     * in [MatterCore]) encoded as a string (11 characters) or the Passcode portion of the Manual Pairing Code (when
     * less than 11 characters) .
     * The server shall implement rate limiting to prevent brute force attacks. No more than 10 unique requests in a 10
     * minute period shall be allowed; a command response status of FAILURE should sent for additional commands received
     * within the 10 minute period. Because access to this command is limited to nodes with Admin-level access, and the
     * user is prompted for consent prior to Commissioning, there are in place multiple obstacles to successfully
     * mounting a brute force attack. A Content App that supports this command shall ensure that the Temporary Account
     * Identifier used by its clients is not valid for more than 10 minutes.
     */
    public static ClusterCommand getSetupPin(String tempAccountIdentifier) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (tempAccountIdentifier != null) {
            map.put("tempAccountIdentifier", tempAccountIdentifier);
        }
        return new ClusterCommand("getSetupPin", map);
    }

    /**
     * The purpose of this command is to allow the Content App to assume the user account of a given Commissionee by
     * leveraging the Setup PIN code input by the user during the commissioning process.
     * For example, a Video Player with a Content App Platform may invoke this command on one of its Content App
     * endpoints after the commissioning has completed of a Phone App made by the same vendor as the Content App. The
     * Content App may determine whether the Temporary Account Identifier maps to an account with a corresponding Setup
     * PIN and, if so, it may automatically login to the account for the corresponding user. The end result is that a
     * user performs commissioning of a Phone App to a Video Player by inputting the Setup PIN for the Phone App into
     * the Video Player UX. Once commissioning has completed, the Video Player invokes this command to allow the
     * corresponding Content App to assume the same user account as the Phone App.
     * The verification of Setup PIN for the given Temporary Account Identifier is determined by the Content App using a
     * method which is outside the scope of this specification and will typically involve a central service which is in
     * communication with both the Content App and the Commissionee. Implementations of such a service should impose
     * aggressive time outs for any mapping of Temporary Account Identifier to Setup PIN in order to prevent accidental
     * login due to delayed invocation.
     * Upon receipt, the Content App checks if the account associated with the client’s Temp Account Identifier has a
     * current active Setup PIN with the given value. If the Setup PIN is valid for the user account associated with the
     * Temp Account Identifier, then the Content App may make that user account active.
     * The Temporary Account Identifier for a Commissionee may be populated with the Rotating ID field of the client’s
     * commissionable node advertisement encoded as an octet string where the octets of the Rotating Device Identifier
     * are encoded as 2-character sequences by representing each octet’s value as a 2-digit hexadecimal number, using
     * uppercase letters.
     * The Setup PIN for a Commissionee may be populated with the Manual Pairing Code encoded as a string of decimal
     * numbers (11 characters) or the Passcode portion of the Manual Pairing Code encoded as a string of decimal numbers
     * (8 characters) .
     * The server shall implement rate limiting to prevent brute force attacks. No more than 10 unique requests in a 10
     * minute period shall be allowed; a command response status of FAILURE should sent for additional commands received
     * within the 10 minute period. Because access to this command is limited to nodes with Admin-level access, and the
     * user is involved when obtaining the SetupPIN, there are in place multiple obstacles to successfully mounting a
     * brute force attack. A Content App that supports this command shall ensure that the Temporary Account Identifier
     * used by its clients is not valid for more than 10 minutes.
     */
    public static ClusterCommand login(String tempAccountIdentifier, String setupPin, BigInteger node) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (tempAccountIdentifier != null) {
            map.put("tempAccountIdentifier", tempAccountIdentifier);
        }
        if (setupPin != null) {
            map.put("setupPin", setupPin);
        }
        if (node != null) {
            map.put("node", node);
        }
        return new ClusterCommand("login", map);
    }

    /**
     * The purpose of this command is to instruct the Content App to clear the current user account. This command SHOULD
     * be used by clients of a Content App to indicate the end of a user session.
     */
    public static ClusterCommand logout(BigInteger node) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (node != null) {
            map.put("node", node);
        }
        return new ClusterCommand("logout", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
