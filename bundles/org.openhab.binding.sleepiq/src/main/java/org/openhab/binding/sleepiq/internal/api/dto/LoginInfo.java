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
package org.openhab.binding.sleepiq.internal.api.dto;

/**
 * The {@link LoginInfo} holds the LoginInfo response from the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class LoginInfo {
    private String userId;
    private String key;
    private Long registrationState;
    private Long edpLoginStatus;
    private String edpLoginMessage;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LoginInfo withUserId(String userId) {
        setUserId(userId);
        return this;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LoginInfo withKey(String key) {
        setKey(key);
        return this;
    }

    public Long getRegistrationState() {
        return registrationState;
    }

    public void setRegistrationState(Long registrationState) {
        this.registrationState = registrationState;
    }

    public LoginInfo withRegistrationState(Long registrationState) {
        setRegistrationState(registrationState);
        return this;
    }

    public Long getEdpLoginStatus() {
        return edpLoginStatus;
    }

    public void setEdpLoginStatus(Long edpLoginStatus) {
        this.edpLoginStatus = edpLoginStatus;
    }

    public LoginInfo withEdpLoginStatus(Long edpLoginStatus) {
        setEdpLoginStatus(edpLoginStatus);
        return this;
    }

    public String getEdpLoginMessage() {
        return edpLoginMessage;
    }

    public void setEdpLoginMessage(String edpLoginMessage) {
        this.edpLoginMessage = edpLoginMessage;
    }

    public LoginInfo withEdpLoginMessage(String edpLoginMessage) {
        setEdpLoginMessage(edpLoginMessage);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + (int) (registrationState ^ (registrationState >>> 32));
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LoginInfo)) {
            return false;
        }
        LoginInfo other = (LoginInfo) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (!registrationState.equals(other.registrationState)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LoginInfo [userId=");
        builder.append(userId);
        builder.append(", key=");
        builder.append(key);
        builder.append(", registrationState=");
        builder.append(registrationState);
        builder.append(", edpLoginStatus=");
        builder.append(edpLoginStatus);
        builder.append(", edpLoginMessage=");
        builder.append(edpLoginMessage);
        builder.append("]");
        return builder.toString();
    }
}
