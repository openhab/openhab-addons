/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
    public static final String AUTHORIZATION_STATUS_UNKNOWN = "unknown";
    public static final String AUTHORIZATION_STATUS_PENDING = "pending";
    public static final String AUTHORIZATION_STATUS_GRANTED = "granted";

    private String status;
    private String challenge;

    public String getStatus() {
        return status;
    }

    public String getChallenge() {
        return challenge;
    }
}
