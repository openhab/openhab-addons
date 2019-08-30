/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.gpstracker.internal.config;

/**
 * The {@link Life360Config} class is a configuration POJO for Life360 bridge.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class Life360Config {
    private String userName;
    private String password;
    private String authToken;
    private Integer refreshPeriod;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Integer getRefreshPeriod() {
        return refreshPeriod;
    }

    public void setRefreshPeriod(Integer refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }
}
