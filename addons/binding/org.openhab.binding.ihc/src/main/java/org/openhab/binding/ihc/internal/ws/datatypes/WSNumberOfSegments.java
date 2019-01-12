/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.datatypes;

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
     * @param value
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
        } catch (Exception e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }
}
