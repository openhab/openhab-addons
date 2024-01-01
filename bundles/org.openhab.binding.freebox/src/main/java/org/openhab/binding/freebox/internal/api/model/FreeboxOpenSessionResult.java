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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxOpenSessionResult} is the Java class used to map the
 * structure used by the response of the open session API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxOpenSessionResult {
    private String sessionToken;
    private String challenge;
    private FreeboxPermissions permissions;

    public String getSessionToken() {
        return sessionToken;
    }

    public String getChallenge() {
        return challenge;
    }

    public FreeboxPermissions getPermissions() {
        return permissions;
    }
}
