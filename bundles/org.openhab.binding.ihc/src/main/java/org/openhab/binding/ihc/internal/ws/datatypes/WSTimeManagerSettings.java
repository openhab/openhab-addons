/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Class for WSTimeManagerSettings complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSTimeManagerSettings {

    private boolean synchroniseTimeAgainstServer;
    private boolean useDST;
    private int gmtOffsetInHours;
    private String serverName;
    private int syncIntervalInHours;
    private WSDate timeAndDateInUTC;

    public WSTimeManagerSettings() {
    }

    public WSTimeManagerSettings(boolean synchroniseTimeAgainstServer, boolean useDST, int gmtOffsetInHours,
            String serverName, int syncIntervalInHours, WSDate timeAndDateInUTC) {
        this.synchroniseTimeAgainstServer = synchroniseTimeAgainstServer;
        this.useDST = useDST;
        this.gmtOffsetInHours = gmtOffsetInHours;
        this.serverName = serverName;
        this.syncIntervalInHours = syncIntervalInHours;
        this.timeAndDateInUTC = timeAndDateInUTC;
    }

    public boolean getSynchroniseTimeAgainstServer() {
        return synchroniseTimeAgainstServer;
    }

    public void setSynchroniseTimeAgainstServer(boolean synchroniseTimeAgainstServer) {
        this.synchroniseTimeAgainstServer = synchroniseTimeAgainstServer;
    }

    public boolean getUseDST() {
        return useDST;
    }

    public void setUseDST(boolean useDST) {
        this.useDST = useDST;
    }

    public int getGmtOffsetInHours() {
        return gmtOffsetInHours;
    }

    public void setGmtOffsetInHours(int gmtOffsetInHours) {
        this.gmtOffsetInHours = gmtOffsetInHours;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getSyncIntervalInHours() {
        return syncIntervalInHours;
    }

    public void setSyncIntervalInHours(int syncIntervalInHours) {
        this.syncIntervalInHours = syncIntervalInHours;
    }

    public WSDate getTimeAndDateInUTC() {
        return timeAndDateInUTC;
    }

    public void setTimeAndDateInUTC(WSDate timeAndDateInUTC) {
        this.timeAndDateInUTC = timeAndDateInUTC;
    }

    public WSTimeManagerSettings parseXMLData(String data) throws IhcExecption {
        try {
            String value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:synchroniseTimeAgainstServer");
            setSynchroniseTimeAgainstServer(Boolean.parseBoolean(value));

            value = XPathUtils.parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:useDST");
            setUseDST(Boolean.parseBoolean(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:gmtOffsetInHours");
            setGmtOffsetInHours(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:serverName");
            setServerName(value);

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:syncIntervalInHours");
            setSyncIntervalInHours(Integer.parseInt(value));

            WSDate timeAndDateInUTC = new WSDate();

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:timeAndDateInUTC/ns1:day");
            timeAndDateInUTC.setDay(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:timeAndDateInUTC/ns1:monthWithJanuaryAsOne");
            timeAndDateInUTC.setMonthWithJanuaryAsOne(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:timeAndDateInUTC/ns1:hours");
            timeAndDateInUTC.setHours(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:timeAndDateInUTC/ns1:minutes");
            timeAndDateInUTC.setMinutes(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:timeAndDateInUTC/ns1:seconds");
            timeAndDateInUTC.setSeconds(Integer.parseInt(value));

            value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getSettings1/ns1:timeAndDateInUTC/ns1:year");
            timeAndDateInUTC.setYear(Integer.parseInt(value));

            setTimeAndDateInUTC(timeAndDateInUTC);

            return this;
        } catch (IOException | XPathExpressionException | NumberFormatException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "[ synchroniseTimeAgainstServer=%b, useDST=%b, gmtOffsetInHours=%d, serverName=%s, syncIntervalInHours=%d, timeAndDateInUTC=%s ]",
                synchroniseTimeAgainstServer, useDST, gmtOffsetInHours, serverName, syncIntervalInHours,
                timeAndDateInUTC.getAsLocalDateTime());
    }
}
