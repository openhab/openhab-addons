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
package org.openhab.binding.unifiaccess.internal.dto;

/**
 * NFC Card model.
 * Represents a single NFC card and its assignment to a user.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class NfcCard {
    public String alias;
    public String cardType;
    public String displayId;
    public String note;
    public String status;
    public String token;
    public UserSummary user;
    public String userId;
    public String userType;

    public static class UserSummary {
        public String firstName;
        public String id;
        public String lastName;
        public String name;
    }

    /* ----------------- Helpers ----------------- */

    /**
     * Returns true if the card is assigned to any user.
     */
    public boolean isAssigned() {
        return userId != null && !userId.isBlank();
    }
}
