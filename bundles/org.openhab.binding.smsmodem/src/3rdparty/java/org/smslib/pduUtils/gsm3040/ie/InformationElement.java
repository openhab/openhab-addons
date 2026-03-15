package org.smslib.pduUtils.gsm3040.ie;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.smslib.pduUtils.gsm3040.PduUtils;

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
public class InformationElement {
    private byte identifier;

    private byte[] data;

    // iei
    // iel (implicit length of data)
    // ied (raw ie data)
    InformationElement(byte id, byte[] ieData) {
        this.identifier = id;
        this.data = ieData;
    }

    // for outgoing messages
    void initialize(byte id, byte[] ieData) {
        this.identifier = id;
        this.data = ieData;
    }

    public int getIdentifier() {
        return (this.identifier & 0xFF);
    }

    public int getLength() {
        return this.data.length;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName() + "[");
        sb.append(PduUtils.byteToPdu(this.identifier));
        sb.append(", ");
        sb.append(PduUtils.byteToPdu(this.data.length));
        sb.append(", ");
        sb.append(PduUtils.bytesToPdu(this.data));
        sb.append("]");
        return sb.toString();
    }
}
