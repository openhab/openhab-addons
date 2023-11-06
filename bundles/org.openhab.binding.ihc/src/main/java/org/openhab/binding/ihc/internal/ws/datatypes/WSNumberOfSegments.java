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

import javax.xml.xpath.XPathExpressionException;

import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Class for WSNumberOfSegments complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSNumberOfSegments {

    private int segments;

    public WSNumberOfSegments() {
    }

    public WSNumberOfSegments(int segments) {
        this.segments = segments;
    }

    /**
     * Gets the number of segments.
     *
     * @return segmentation size
     */
    public int getNumberOfSegments() {
        return segments;
    }

    /**
     * Sets the number of segmentations.
     *
     * @param segments
     */
    public void setNumberOfSegments(int segments) {
        this.segments = segments;
    }

    public WSNumberOfSegments parseXMLData(String data) throws IhcExecption {
        try {
            String value = XPathUtils.parseXMLValue(data,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getIHCProjectNumberOfSegments1");
            setNumberOfSegments(Integer.parseInt(value));
            return this;
        } catch (IOException | XPathExpressionException | NumberFormatException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }
}
