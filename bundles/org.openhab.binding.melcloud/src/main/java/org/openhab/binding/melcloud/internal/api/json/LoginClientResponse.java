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
package org.openhab.binding.melcloud.internal.api.json;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * The {@link LoginClientResponse} is responsible of JSON data For MELCloud API
 * Response Data of Login.
 * Generated with jsonschema2pojo
 *
 * @author Luca Calcaterra - Initial contribution
 */
public class LoginClientResponse {

    @Expose
    private Object errorId;

    @Expose
    private Object errorMessage;

    @Expose
    private Integer loginStatus;

    @Expose
    private Integer userId;

    @Expose
    private Object randomKey;

    @Expose
    private Object appVersionAnnouncement;

    @Expose
    private LoginData loginData;

    @Expose
    private List<Object> listPendingInvite = null;

    @Expose
    private List<Object> listOwnershipChangeRequest = null;

    @Expose
    private List<Object> listPendingAnnouncement = null;

    @Expose
    private Integer loginMinutes;

    @Expose
    private Integer loginAttempts;

    public Object getErrorId() {
        return errorId;
    }

    public void setErrorId(Object errorId) {
        this.errorId = errorId;
    }

    public Object getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Object errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(Integer loginStatus) {
        this.loginStatus = loginStatus;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Object getRandomKey() {
        return randomKey;
    }

    public void setRandomKey(Object randomKey) {
        this.randomKey = randomKey;
    }

    public Object getAppVersionAnnouncement() {
        return appVersionAnnouncement;
    }

    public void setAppVersionAnnouncement(Object appVersionAnnouncement) {
        this.appVersionAnnouncement = appVersionAnnouncement;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
    }

    public List<Object> getListPendingInvite() {
        return listPendingInvite;
    }

    public void setListPendingInvite(List<Object> listPendingInvite) {
        this.listPendingInvite = listPendingInvite;
    }

    public List<Object> getListOwnershipChangeRequest() {
        return listOwnershipChangeRequest;
    }

    public void setListOwnershipChangeRequest(List<Object> listOwnershipChangeRequest) {
        this.listOwnershipChangeRequest = listOwnershipChangeRequest;
    }

    public List<Object> getListPendingAnnouncement() {
        return listPendingAnnouncement;
    }

    public void setListPendingAnnouncement(List<Object> listPendingAnnouncement) {
        this.listPendingAnnouncement = listPendingAnnouncement;
    }

    public Integer getLoginMinutes() {
        return loginMinutes;
    }

    public void setLoginMinutes(Integer loginMinutes) {
        this.loginMinutes = loginMinutes;
    }

    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
}
