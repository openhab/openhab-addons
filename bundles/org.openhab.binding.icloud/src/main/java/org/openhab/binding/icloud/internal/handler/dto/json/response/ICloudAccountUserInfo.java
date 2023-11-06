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
package org.openhab.binding.icloud.internal.handler.dto.json.response;

/**
 * Serializable class to parse json response received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudAccountUserInfo {
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
