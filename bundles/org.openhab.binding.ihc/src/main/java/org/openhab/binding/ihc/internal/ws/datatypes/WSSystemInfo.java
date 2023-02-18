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
package org.openhab.binding.ihc.internal.ws.datatypes;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import javax.xml.xpath.XPathExpressionException;

import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Class for WSProjectInfo complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSystemInfo {

    private long uptime;
    private ZonedDateTime realtimeclock;
    private String serialNumber;
    private String brand;
    private String version;
    private String hwRevision;
    private ZonedDateTime swDate;
    private boolean applicationIsWithoutViewer;
    private String productionDate;
    private String datalineVersion;
    private String rfModuleSoftwareVersion;
    private String rfModuleSerialNumber;

    public WSSystemInfo() {
    }

    public WSSystemInfo(long uptime, ZonedDateTime realtimeclock, String serialNumber, String brand, String version,
            String hwRevision, ZonedDateTime swDate, boolean applicationIsWithoutViewer, String productionDate,
            String datalineVersion, String rfModuleSoftwareVersion, String rfModuleSerialNumber) {
        this.uptime = uptime;
        this.realtimeclock = realtimeclock;
        this.serialNumber = serialNumber;
        this.brand = brand;
        this.version = version;
        this.hwRevision = hwRevision;
        this.swDate = swDate;
        this.applicationIsWithoutViewer = applicationIsWithoutViewer;
        this.productionDate = productionDate;
        this.datalineVersion = datalineVersion;
        this.rfModuleSoftwareVersion = rfModuleSoftwareVersion;
        this.rfModuleSerialNumber = rfModuleSerialNumber;
    }

    /**
     * Gets the uptime value for this WSSystemInfo.
     *
     * @return uptime in milliseconds
     */
    public long getUptime() {
        return uptime;
    }

    /**
     * Sets the uptime value for this WSSystemInfo.
     *
     * @param uptime uptime in milliseconds
     */
    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    /**
     * Gets the RealTimeClock value for this WSSystemInfo.
     *
     * @return Real Time Clock
     */
    public ZonedDateTime getRealTimeClock() {
        return realtimeclock;
    }

    /**
     * Sets the RealTimeClock value for this WSSystemInfo.
     *
     * @param RealTimeClock
     */
    public void setRealTimeClock(ZonedDateTime realtimeclock) {
        this.realtimeclock = realtimeclock;
    }

    /**
     * Gets the SerialNumber value for this WSSystemInfo.
     *
     * @return SerialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the SerialNumber value for this WSSystemInfo.
     *
     * @param SerialNumber
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Gets the brand value for this WSSystemInfo.
     *
     * @return brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Sets the brand value for this WSSystemInfo.
     *
     * @param brand
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Gets the version value for this WSSystemInfo.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version value for this WSSystemInfo.
     *
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the hwRevision value for this WSSystemInfo.
     *
     * @return hwRevision
     */
    public String getHwRevision() {
        return hwRevision;
    }

    /**
     * Sets the hwRevision value for this WSSystemInfo.
     *
     * @param hwRevision
     */
    public void setHwRevision(String hwRevision) {
        this.hwRevision = hwRevision;
    }

    /**
     * Gets the swDate value for this WSSystemInfo.
     *
     * @return swDate
     */
    public ZonedDateTime getSwDate() {
        return swDate;
    }

    /**
     * Sets the swDate value for this WSSystemInfo.
     *
     * @param swDate
     */
    public void setSwDate(ZonedDateTime swDate) {
        this.swDate = swDate;
    }

    /**
     * Gets the applicationIsWithoutViewer value for this WSSystemInfo.
     *
     * @return applicationIsWithoutViewer
     */
    public boolean getApplicationIsWithoutViewer() {
        return applicationIsWithoutViewer;
    }

    /**
     * Sets the applicationIsWithoutViewer value for this WSSystemInfo.
     *
     * @param applicationIsWithoutViewer
     */
    public void setApplicationIsWithoutViewer(boolean applicationIsWithoutViewer) {
        this.applicationIsWithoutViewer = applicationIsWithoutViewer;
    }

    /**
     * Gets the productionDate value for this WSSystemInfo.
     *
     * @return productionDate
     */
    public String getProductionDate() {
        return productionDate;
    }

    /**
     * Sets the productionDate value for this WSSystemInfo.
     *
     * @param productionDate
     */
    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    /**
     * Gets the datalineVersion value for this WSSystemInfo.
     *
     * @return datalineVersion
     */
    public String getDatalineVersion() {
        return datalineVersion;
    }

    /**
     * Sets the datalineVersion value for this WSSystemInfo.
     *
     * @param datalineVersion
     */
    public void setDatalineVersion(String datalineVersion) {
        this.datalineVersion = datalineVersion;
    }

    /**
     * Gets the rfModuleSoftwareVersion value for this WSSystemInfo.
     *
     * @return rfModuleSoftwareVersion
     */
    public String getRfModuleSoftwareVersion() {
        return rfModuleSoftwareVersion;
    }

    /**
     * Sets the rfModuleSoftwareVersion value for this WSSystemInfo.
     *
     * @param rfModuleSoftwareVersion
     */
    public void setRfModuleSoftwareVersion(String rfModuleSoftwareVersion) {
        this.rfModuleSoftwareVersion = rfModuleSoftwareVersion;
    }

    /**
     * Gets the rfModuleSerialNumber value for this WSSystemInfo.
     *
     * @return rfModuleSerialNumber
     */
    public String getRfModuleSerialNumber() {
        return rfModuleSerialNumber;
    }

    /**
     * Sets the rfModuleSerialNumber value for this WSSystemInfo.
     *
     * @param rfModuleSerialNumber
     */
    public void setRfModuleSerialNumber(String rfModuleSerialNumber) {
        this.rfModuleSerialNumber = rfModuleSerialNumber;
    }

    public WSSystemInfo parseXMLData(String data) throws IhcExecption {
        try {
            String value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:uptime");
            setUptime(Long.parseLong(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:serialNumber");
            setSerialNumber(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:realtimeclock");
            setRealTimeClock(ZonedDateTime.parse(value));

            value = XPathUtils.parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:brand");
            setBrand(value);

            value = XPathUtils.parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:version");
            setVersion(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:hwRevision");
            setHwRevision(value);

            value = XPathUtils.parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:swDate");
            setSwDate(ZonedDateTime.parse(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:applicationIsWithoutViewer");
            setApplicationIsWithoutViewer(Boolean.parseBoolean(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:productionDate");
            setProductionDate(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:datalineVersion");
            setDatalineVersion(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:rfModuleSoftwareVersion");
            setRfModuleSoftwareVersion(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:rfModuleSerialNumber");
            setRfModuleSerialNumber(value);

            return this;
        } catch (IOException | XPathExpressionException | NumberFormatException | DateTimeParseException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "[ uptime=%d, realtimeclock=%s, serialNumber=%s, brand=%s, version=%s, hwRevision=%s, swDate=%s, applicationIsWithoutViewer=%b, productionDate=%s, datalineVersion=%s, rfModuleSoftwareVersion=%s, rfModuleSerialNumber=%s ]",
                uptime, realtimeclock, serialNumber, brand, version, hwRevision, swDate, applicationIsWithoutViewer,
                productionDate, datalineVersion, rfModuleSoftwareVersion, rfModuleSerialNumber);
    }
}
