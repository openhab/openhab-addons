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
package org.openhab.binding.ihc.internal.ws.datatypes;

/**
 * Class for WSUser complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSUser {

    protected WSDate createdDate;
    protected WSDate loginDate;
    protected String username;
    protected String password;
    protected String email;
    protected String firstname;
    protected String lastname;
    protected String phone;
    protected WSUserGroup group;
    protected String project;

    public WSUser() {
    }

    public WSUser(WSDate createdDate, WSDate loginDate, String username, String password, String email,
            String firstname, String lastname, String phone, WSUserGroup group, String project) {
        this.createdDate = createdDate;
        this.loginDate = loginDate;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.group = group;
        this.project = project;
    }

    /**
     * Gets the value of the createdDate property.
     *
     * @return possible object is {@link WSDate }
     *
     */
    public WSDate getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets the value of the createdDate property.
     *
     * @param value allowed object is {@link WSDate }
     *
     */
    public void setCreatedDate(WSDate value) {
        this.createdDate = value;
    }

    /**
     * Gets the value of the loginDate property.
     *
     * @return possible object is {@link WSDate }
     *
     */
    public WSDate getLoginDate() {
        return loginDate;
    }

    /**
     * Sets the value of the loginDate property.
     *
     * @param value allowed object is {@link WSDate }
     *
     */
    public void setLoginDate(WSDate value) {
        this.loginDate = value;
    }

    /**
     * Gets the value of the username property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the email property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the firstname property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Sets the value of the firstname property.
     */
    public void setFirstname(String value) {
        this.firstname = value;
    }

    /**
     * Gets the value of the lastname property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Sets the value of the lastname property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setLastname(String value) {
        this.lastname = value;
    }

    /**
     * Gets the value of the phone property.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     *
     * @param value allowed object is {@link String }
     *            {@link String }
     *
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the group property.
     *
     * @return possible object is {@link WSUserGroup }
     *
     */
    public WSUserGroup getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     *
     * @param value allowed object is {@link WSUserGroup }
     *
     */
    public void setGroup(WSUserGroup value) {
        this.group = value;
    }

    /**
     * Gets the value of the project property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getProject() {
        return project;
    }

    /**
     * Sets the value of the project property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setProject(String value) {
        this.project = value;
    }
}
