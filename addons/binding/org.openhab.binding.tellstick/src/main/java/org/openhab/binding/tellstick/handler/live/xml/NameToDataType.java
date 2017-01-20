/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.handler.live.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.tellstick.enums.DataType;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland
 */
public class NameToDataType extends XmlAdapter<String, DataType> {
    @Override
    public DataType unmarshal(String v) throws Exception {
        switch (v) {
            case "temp":
                return DataType.TEMPERATURE;
            case "humidity":
                return DataType.HUMIDITY;
            default:
                return null;
        }
    }

    @Override
    public String marshal(DataType v) throws Exception {
        switch (v) {
            case TEMPERATURE:
                return "temp";
            case HUMIDITY:
                return "humidity";
            default:
                return null;
        }
    }

}