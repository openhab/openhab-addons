package org.smslib.pduUtils.gsm3040.ie;

import org.eclipse.jdt.annotation.NonNullByDefault;

//PduUtils Library - A Java library for generating GSM 3040 Protocol Data Units (PDUs)
//
//Copyright (C) 2008, Ateneo Java Wireless Competency Center/Blueblade Technologies, Philippines.
//PduUtils is distributed under the terms of the Apache License version 2.0
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public class InformationElementFactory {
    // used to determine what InformationElement to use based on bytes from a UDH
    // assumes the supplied bytes are correct
    public static InformationElement createInformationElement(int id, byte[] data) {
        byte iei = (byte) (id & 0xFF);
        switch (iei) {
            case ConcatInformationElement.CONCAT_8BIT_REF:
            case ConcatInformationElement.CONCAT_16BIT_REF:
                return new ConcatInformationElement(iei, data);
            case PortInformationElement.PORT_16BIT:
                return new PortInformationElement(iei, data);
            default:
                return new InformationElement(iei, data);
        }
    }

    public static ConcatInformationElement generateConcatInfo(int mpRefNo, int partNo) {
        ConcatInformationElement concatInfo = new ConcatInformationElement(
                ConcatInformationElement.getDefaultConcatType(), mpRefNo, 1, partNo);
        return concatInfo;
    }

    public static PortInformationElement generatePortInfo(int destPort, int srcPort) {
        PortInformationElement portInfo = new PortInformationElement(PortInformationElement.PORT_16BIT, destPort,
                srcPort);
        return portInfo;
    }
}
