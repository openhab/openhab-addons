/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api.manager;

/**
 * Configuration for the {@link EnergenieApiManager}
 *
 * @author Svilen Valkanov - Initial contribution
 *
 */
public class EnergenieApiConfiguration {

    private String userName;
    private String password;

    /**
     * Creates a new {@link EnergenieApiConfiguration} that contains the username and the password needed for making the
     * requests
     *
     * @param userName user's email address used for the registration in the Mi|Home Web portal
     * @param password user's password
     */
    public EnergenieApiConfiguration(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
