/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json;

/**
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class UserInfo {
    private int accountFormatter;

    private String firstName;

    private boolean hasMembers;

    private String lastName;

    private Object membersInfo;

    public int getAccountFormatter() {
        return this.accountFormatter;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public boolean getHasMembers() {
        return this.hasMembers;
    }

    public String getLastName() {
        return this.lastName;
    }

    public Object getMembersInfo() {
        return this.membersInfo;
    }

    public void setAccountFormatter(int accountFormatter) {
        this.accountFormatter = accountFormatter;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setHasMembers(boolean hasMembers) {
        this.hasMembers = hasMembers;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMembersInfo(Object membersInfo) {
        this.membersInfo = membersInfo;
    }
}