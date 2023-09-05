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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxAuthorizationStatus} is the Java class used to map the
 * structure used by the response of the track authorization progress API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxAuthorizationStatus {

    private enum AuthorizationStatus {
        UNKNOWN("unknown"),
        PENDING("pending"),
        TIMEOUT("timeout"),
        GRANTED("granted"),
        DENIED("denied");

        private String status;

        private AuthorizationStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    private String status;
    private String challenge;

    public boolean isStatusPending() {
        return AuthorizationStatus.PENDING.getStatus().equalsIgnoreCase(status);
    }

    public boolean isStatusGranted() {
        return AuthorizationStatus.GRANTED.getStatus().equalsIgnoreCase(status);
    }

    public String getStatus() {
        return status;
    }

    public String getChallenge() {
        return challenge;
    }
}
