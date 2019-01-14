/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    private static enum AuthorizationStatus {
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
