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
 * Class for WSProjectInfo complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSProjectInfo {

    private int visualMinorVersion;
    private int visualMajorVersion;
    private int projectMajorRevision;
    private int projectMinorRevision;
    private WSDate lastmodified;
    private String projectNumber;
    private String customerName;
    private String installerName;

    public WSProjectInfo() {
    }

    public WSProjectInfo(int visualMinorVersion, int visualMajorVersion, int projectMajorRevision,
            int projectMinorRevision, WSDate lastmodified, String projectNumber, String customerName,
            String installerName) {
        this.visualMinorVersion = visualMinorVersion;
        this.visualMajorVersion = visualMajorVersion;
        this.projectMajorRevision = projectMajorRevision;
        this.projectMinorRevision = projectMinorRevision;
        this.lastmodified = lastmodified;
        this.projectNumber = projectNumber;
        this.customerName = customerName;
        this.installerName = installerName;
    }

    /**
     * Gets the visualMinorVersion value for this WSProjectInfo.
     *
     * @return visualMinorVersion
     */
    public int getVisualMinorVersion() {
        return visualMinorVersion;
    }

    /**
     * Sets the visualMinorVersion value for this WSProjectInfo.
     *
     * @param visualMinorVersion
     */
    public void setVisualMinorVersion(int visualMinorVersion) {
        this.visualMinorVersion = visualMinorVersion;
    }

    /**
     * Gets the visualMajorVersion value for this WSProjectInfo.
     *
     * @return visualMajorVersion
     */
    public int getVisualMajorVersion() {
        return visualMajorVersion;
    }

    /**
     * Sets the visualMajorVersion value for this WSProjectInfo.
     *
     * @param visualMajorVersion
     */
    public void setVisualMajorVersion(int visualMajorVersion) {
        this.visualMajorVersion = visualMajorVersion;
    }

    /**
     * Gets the projectMajorRevision value for this WSProjectInfo.
     *
     * @return projectMajorRevision
     */
    public int getProjectMajorRevision() {
        return projectMajorRevision;
    }

    /**
     * Sets the projectMajorRevision value for this WSProjectInfo.
     *
     * @param projectMajorRevision
     */
    public void setProjectMajorRevision(int projectMajorRevision) {
        this.projectMajorRevision = projectMajorRevision;
    }

    /**
     * Gets the projectMinorRevision value for this WSProjectInfo.
     *
     * @return projectMinorRevision
     */
    public int getProjectMinorRevision() {
        return projectMinorRevision;
    }

    /**
     * Sets the projectMinorRevision value for this WSProjectInfo.
     *
     * @param projectMinorRevision
     */
    public void setProjectMinorRevision(int projectMinorRevision) {
        this.projectMinorRevision = projectMinorRevision;
    }

    /**
     * Gets the lastmodified value for this WSProjectInfo.
     *
     * @return lastmodified
     */
    public WSDate getLastmodified() {
        return lastmodified;
    }

    /**
     * Sets the lastmodified value for this WSProjectInfo.
     *
     * @param lastmodified
     */
    public void setLastmodified(WSDate lastmodified) {
        this.lastmodified = lastmodified;
    }

    /**
     * Gets the projectNumber value for this WSProjectInfo.
     *
     * @return projectNumber
     */
    public java.lang.String getProjectNumber() {
        return projectNumber;
    }

    /**
     * Sets the projectNumber value for this WSProjectInfo.
     *
     * @param projectNumber
     */
    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    /**
     * Gets the customerName value for this WSProjectInfo.
     *
     * @return customerName
     */
    public java.lang.String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the customerName value for this WSProjectInfo.
     *
     * @param customerName
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Gets the installerName value for this WSProjectInfo.
     *
     * @return installerName
     */
    public java.lang.String getInstallerName() {
        return installerName;
    }

    /**
     * Sets the installerName value for this WSProjectInfo.
     *
     * @param installerName
     */
    public void setInstallerName(String installerName) {
        this.installerName = installerName;
    }

    public WSProjectInfo parseXMLData(String data) throws IhcExecption {
        try {
            String value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:visualMinorVersion");
            setVisualMinorVersion(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:visualMajorVersion");
            setVisualMajorVersion(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:projectMajorRevision");
            setProjectMajorRevision(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:projectMinorRevision");
            setProjectMinorRevision(Integer.parseInt(value));

            WSDate lastmodified = new WSDate();

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:lastmodified/ns1:day");
            lastmodified.setDay(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:lastmodified/ns1:monthWithJanuaryAsOne");
            lastmodified.setMonthWithJanuaryAsOne(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:lastmodified/ns1:hours");
            lastmodified.setHours(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:lastmodified/ns1:minutes");
            lastmodified.setMinutes(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:lastmodified/ns1:seconds");
            lastmodified.setSeconds(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:lastmodified/ns1:year");
            lastmodified.setYear(Integer.parseInt(value));

            setLastmodified(lastmodified);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:projectNumber");
            setProjectNumber(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:customerName");
            setCustomerName(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getProjectInfo1/ns1:installerName");
            setInstallerName(value);

            return this;
        } catch (IOException | XPathExpressionException | NumberFormatException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "[ visualMinorVersion=%d, visualMajorVersion=%d, projectMajorRevision=%d, projectMinorRevision=%d, lastmodified=%s, projectNumber=%s, customerName=%s, installerName=%s ]",
                visualMinorVersion, visualMajorVersion, projectMajorRevision, projectMinorRevision,
                lastmodified.getAsLocalDateTime(), projectNumber, customerName, installerName);
    }
}
