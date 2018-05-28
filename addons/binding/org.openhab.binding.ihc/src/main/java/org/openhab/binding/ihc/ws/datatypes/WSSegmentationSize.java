/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.datatypes;

import org.openhab.binding.ihc.ws.exeptions.IhcExecption;

/**
 * Java class for WSSegmentationSize complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSegmentationSize extends WSBaseDataType {

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
        String value = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getIHCProjectSegmentationSize1");
        setSegmentationSize(Integer.parseInt(value));
        return this;
    }
}
