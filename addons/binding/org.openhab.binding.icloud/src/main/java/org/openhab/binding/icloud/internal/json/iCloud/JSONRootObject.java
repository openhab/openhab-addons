/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.iCloud;

import java.util.ArrayList;

public class JSONRootObject {
    private UserInfo userInfo;

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    private ServerContext serverContext;

    public ServerContext getServerContext() {
        return this.serverContext;
    }

    public void setServerContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    private Object alert;

    public Object getAlert() {
        return this.alert;
    }

    public void setAlert(Object alert) {
        this.alert = alert;
    }

    private Object userPreferences;

    public Object getUserPreferences() {
        return this.userPreferences;
    }

    public void setUserPreferences(Object userPreferences) {
        this.userPreferences = userPreferences;
    }

    private ArrayList<Content> content;

    public ArrayList<Content> getContent() {
        return this.content;
    }

    public void setContent(ArrayList<Content> content) {
        this.content = content;
    }

    private String statusCode;

    public String getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}