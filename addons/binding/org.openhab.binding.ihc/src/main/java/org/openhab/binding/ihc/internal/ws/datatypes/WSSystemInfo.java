/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.datatypes;

import java.time.ZonedDateTime;

import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Class for WSProjectInfo complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSystemInfo extends WSBaseDataType {

    private long uptime;
    private ZonedDateTime realtimeclock;
    private String serialNumber;
    private String brand;
    private String version;
    private String hwRevision;
    private ZonedDateTime swDate;
    private boolean applicationIsWithoutViewer;
    private ZonedDateTime productionDate;

    public WSSystemInfo() {
    }

    public WSSystemInfo(long uptime, ZonedDateTime realtimeclock, String serialNumber, String brand, String version,
            String hwRevision, ZonedDateTime swDate, boolean applicationIsWithoutViewer, ZonedDateTime productionDate) {

        this.uptime = uptime;
        this.realtimeclock = realtimeclock;
        this.serialNumber = serialNumber;
        this.brand = brand;
        this.version = version;
        this.hwRevision = hwRevision;
        this.swDate = swDate;
        this.applicationIsWithoutViewer = applicationIsWithoutViewer;
        this.productionDate = productionDate;
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
    public ZonedDateTime getProductionDate() {
        return productionDate;
    }

    /**
     * Sets the productionDate value for this WSSystemInfo.
     *
     * @param productionDate
     */
    public void setProductionDate(ZonedDateTime productionDate) {
        this.productionDate = productionDate;
    }

    public WSSystemInfo parseXMLData(String data) throws IhcExecption {
        String value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:uptime");
        setUptime(Long.parseLong(value));

        value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:serialNumber");
        setSerialNumber(value);

        value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:realtimeclock");
        setRealTimeClock(ZonedDateTime.parse(value));

        value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:brand");
        setBrand(value);

        value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:version");
        setVersion(value);

        value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:hwRevision");
        setHwRevision(value);

        value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:swDate");
        setSwDate(ZonedDateTime.parse(value));

        value = parseXMLValue(data,
                "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:applicationIsWithoutViewer");
        setApplicationIsWithoutViewer(Boolean.parseBoolean(value));

        value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSystemInfo1/ns1:productionDate");
        setProductionDate(ZonedDateTime.parse(value));

        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "[ uptime=%d, realtimeclock=%s, serialNumber=%s, brand=%s, version=%s, hwRevision=%s, swDate=%s, applicationIsWithoutViewer=%b, productionDate=%s ]",
                uptime, realtimeclock, serialNumber, brand, version, hwRevision, swDate, applicationIsWithoutViewer,
                productionDate);
    }
}
