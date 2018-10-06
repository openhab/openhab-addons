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
 * The {@link FreeboxOpenSessionResponse} is the Java class used to map the
 * structure used by the response of the open session API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxOpenSessionResponse {
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
