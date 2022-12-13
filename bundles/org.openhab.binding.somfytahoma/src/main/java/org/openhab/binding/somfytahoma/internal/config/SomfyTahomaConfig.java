/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants;

/**
 * The {@link SomfyTahomaConfig} is is the base class for configuration
 * information held by devices and modules.
 *
 * @author Ondrej Pecta - Initial contribution
 * @author Laurent Garnier - new parameter portalUrl
 */
@NonNullByDefault
public class SomfyTahomaConfig {
    private String cloudPortal = SomfyTahomaBindingConstants.TAHOMA_PORTAL;
    private String email = "";
    private String password = "";
    private int refresh = 30;
    private int statusTimeout = 300;
    private int retries = 10;
    private int retryDelay = 1000;
    private boolean devMode = false;
    private String pin = "";
    private String ip = "";
    private String token = "";

    public String getCloudPortal() {
        return cloudPortal;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getRefresh() {
        return refresh;
    }

    public int getStatusTimeout() {
        return statusTimeout;
    }

    public int getRetries() {
        return retries;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public String getPin() {
        return pin;
    }

    public String getIp() {
        return ip;
    }

    public String getToken() {
        return token;
    }

    public void setCloudPortal(String cloudPortal) {
        this.cloudPortal = cloudPortal;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
