/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sedif.internal.dto;

import java.util.Hashtable;

/**
 * The {@link AuraCommand} holds a command
 *
 * @author Laurent Arnal - Initial contribution
 */
public class AuraCommand {
    private String nameSpace;
    private String className;
    private String methodName;

    private String userName;
    private String userPassword;
    private Hashtable<String, Object> paramsSub = new Hashtable<String, Object>();

    public AuraCommand(String nameSpace, String className, String methodName) {
        this.nameSpace = nameSpace;
        this.className = className;
        this.methodName = methodName;
    }

    public static AuraCommand make(String nameSpace, String className, String methodName) {
        return new AuraCommand(nameSpace, className, methodName);
    }

    public String getNameSpace() {
        return this.nameSpace;
    }

    public String getClassName() {
        return this.className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUser(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public String getUserPassword() {
        return this.userPassword;
    }

    public Hashtable<String, Object> getParamsSub() {
        return this.paramsSub;
    }
}
