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
 * Class for WSSegmentationSize complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSegmentationSize {

    private int size;

    public WSSegmentationSize() {
    }

    public WSSegmentationSize(int size) {
        this.size = size;
    }

    /**
     * Gets the segmentation size value.
     *
     * @return segmentation size
     */
    public int getSegmentationSize() {
        return size;
    }

    /**
     * Sets the segmentation size value.
     *
     * @param size
     */
    public void setSegmentationSize(int size) {
        this.size = size;
    }

    public WSSegmentationSize parseXMLData(String data) throws IhcExecption {
        try {
            String value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getIHCProjectSegmentationSize1");
            setSegmentationSize(Integer.parseInt(value));
            return this;
        } catch (IOException | XPathExpressionException | NumberFormatException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }
}
