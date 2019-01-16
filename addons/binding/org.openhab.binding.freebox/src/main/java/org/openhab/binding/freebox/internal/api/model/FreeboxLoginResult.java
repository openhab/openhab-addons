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
 * The {@link FreeboxLoginResult} is the Java class used to map the
 * structure used by the response of the login API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLoginResult {
    private Boolean loggedIn;
    private String challenge;

    public Boolean isLoggedIn() {
        return loggedIn;
    }

    public String getChallenge() {
        return challenge;
    }
}
