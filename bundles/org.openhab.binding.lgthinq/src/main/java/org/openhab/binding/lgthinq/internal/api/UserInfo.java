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
package org.openhab.binding.lgthinq.internal.api;

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link UserInfo} User Info (registered in LG Account)
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class UserInfo implements Serializable {

    private String userNumber = "";
    private String userID = "";
    private String userIDType = "";
    private String displayUserID = "";

    public UserInfo() {
    }

    public UserInfo(String userNumber, String userID, String userIDType, String displayUserId) {
        this.userNumber = userNumber;
        this.userID = userID;
        this.userIDType = userIDType;
        this.displayUserID = displayUserId;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserIDType() {
        return userIDType;
    }

    public void setUserIDType(String userIDType) {
        this.userIDType = userIDType;
    }

    public String getDisplayUserID() {
        return displayUserID;
    }

    public void setDisplayUserID(String displayUserID) {
        this.displayUserID = displayUserID;
    }
}
