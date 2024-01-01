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
 * Class for WSFile complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSFile {
    private byte[] data;
    private String filename;

    public WSFile() {
    }

    public WSFile(byte[] data, String filename) {
        this.data = data;
        this.filename = filename;
    }

    /**
     * Gets the data value for this WSFile.
     *
     * @return data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the data value for this WSFile.
     *
     * @param data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the filename value for this WSFile.
     *
     * @return filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename value for this WSFile.
     *
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public WSFile parseXMLData(String data) throws IhcExecption {
        try {
            filename = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getIHCProjectSegment4/ns1:filename");
            this.data = XPathUtils
                    .parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getIHCProjectSegment4/ns1:data")
                    .getBytes();
            return this;
        } catch (IOException | XPathExpressionException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }
}
