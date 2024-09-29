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

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Class for WSLoginResult complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSLoginResult {

    protected WSUser loggedInUser;
    protected boolean loginWasSuccessful;
    protected boolean loginFailedDueToConnectionRestrictions;
    protected boolean loginFailedDueToInsufficientUserRights;
    protected boolean loginFailedDueToAccountInvalid;

    public WSLoginResult() {
    }

    public WSLoginResult(WSUser loggedInUser, boolean loginWasSuccessful,
            boolean loginFailedDueToConnectionRestrictions, boolean loginFailedDueToInsufficientUserRights,
            boolean loginFailedDueToAccountInvalid) {
        this.loggedInUser = loggedInUser;
        this.loginWasSuccessful = loginWasSuccessful;
        this.loginFailedDueToConnectionRestrictions = loginFailedDueToConnectionRestrictions;
        this.loginFailedDueToInsufficientUserRights = loginFailedDueToInsufficientUserRights;
        this.loginFailedDueToAccountInvalid = loginFailedDueToAccountInvalid;
    }

    /**
     * Gets the value of the loggedInUser property.
     *
     * @return possible object is {@link WSUser }
     *
     */
    public WSUser getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Sets the value of the loggedInUser property.
     *
     * @param value allowed object is {@link WSUser }
     *
     */
    public void setLoggedInUser(WSUser value) {
        this.loggedInUser = value;
    }

    /**
     * Gets the value of the loginWasSuccessful property.
     *
     */
    public boolean isLoginWasSuccessful() {
        return loginWasSuccessful;
    }

    /**
     * Sets the value of the loginWasSuccessful property.
     *
     */
    public void setLoginWasSuccessful(boolean value) {
        this.loginWasSuccessful = value;
    }

    /**
     * Gets the value of the loginFailedDueToConnectionRestrictions property.
     *
     */
    public boolean isLoginFailedDueToConnectionRestrictions() {
        return loginFailedDueToConnectionRestrictions;
    }

    /**
     * Sets the value of the loginFailedDueToConnectionRestrictions property.
     *
     */
    public void setLoginFailedDueToConnectionRestrictions(boolean value) {
        this.loginFailedDueToConnectionRestrictions = value;
    }

    /**
     * Gets the value of the loginFailedDueToInsufficientUserRights property.
     *
     */
    public boolean isLoginFailedDueToInsufficientUserRights() {
        return loginFailedDueToInsufficientUserRights;
    }

    /**
     * Sets the value of the loginFailedDueToInsufficientUserRights property.
     *
     */
    public void setLoginFailedDueToInsufficientUserRights(boolean value) {
        this.loginFailedDueToInsufficientUserRights = value;
    }

    /**
     * Gets the value of the loginFailedDueToAccountInvalid property.
     *
     */
    public boolean isLoginFailedDueToAccountInvalid() {
        return loginFailedDueToAccountInvalid;
    }

    /**
     * Sets the value of the loginFailedDueToAccountInvalid property.
     *
     */
    public void setLoginFailedDueToAccountInvalid(boolean value) {
        this.loginFailedDueToAccountInvalid = value;
    }

    public WSLoginResult parseXMLData(String data) throws IhcExecption {
        try {
            loginWasSuccessful = XPathUtils.parseValueToBoolean(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:authenticate2/ns1:loginWasSuccessful");

            loginFailedDueToConnectionRestrictions = XPathUtils.parseValueToBoolean(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:authenticate2/ns1:loginFailedDueToConnectionRestrictions");

            loginFailedDueToInsufficientUserRights = XPathUtils.parseValueToBoolean(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:authenticate2/ns1:loginFailedDueToInsufficientUserRights");

            loginFailedDueToAccountInvalid = XPathUtils.parseValueToBoolean(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:authenticate2/ns1:loginFailedDueToAccountInvalid");

            return this;
        } catch (IOException | XPathExpressionException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }
}
