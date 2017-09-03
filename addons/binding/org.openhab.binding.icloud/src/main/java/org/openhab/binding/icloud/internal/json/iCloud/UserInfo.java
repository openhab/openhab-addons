/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.iCloud;

public class UserInfo {
    private int accountFormatter;

    public int getAccountFormatter() {
        return this.accountFormatter;
    }

    public void setAccountFormatter(int accountFormatter) {
        this.accountFormatter = accountFormatter;
    }

    private String firstName;

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private String lastName;

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    private Object membersInfo;

    public Object getMembersInfo() {
        return this.membersInfo;
    }

    public void setMembersInfo(Object membersInfo) {
        this.membersInfo = membersInfo;
    }

    private boolean hasMembers;

    public boolean getHasMembers() {
        return this.hasMembers;
    }

    public void setHasMembers(boolean hasMembers) {
        this.hasMembers = hasMembers;
    }
}