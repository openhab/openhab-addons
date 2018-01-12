/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json;

import java.util.ArrayList;

/**
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class JSONRootObject {
    private Object alert;

    private ArrayList<DeviceInformation> content;

    private ServerContext serverContext;

    private String statusCode;

    private UserInfo userInfo;

    private Object userPreferences;

    public Object getAlert() {
        return this.alert;
    }

    public ArrayList<DeviceInformation> getContent() {
        return this.content;
    }

    public ServerContext getServerContext() {
        return this.serverContext;
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public Object getUserPreferences() {
        return this.userPreferences;
    }

    public void setAlert(Object alert) {
        this.alert = alert;
    }

    public void setContent(ArrayList<DeviceInformation> content) {
        this.content = content;
    }

    public void setServerContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setUserPreferences(Object userPreferences) {
        this.userPreferences = userPreferences;
    }
}