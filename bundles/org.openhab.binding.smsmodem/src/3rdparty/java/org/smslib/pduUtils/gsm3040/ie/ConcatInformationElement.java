package org.smslib.pduUtils.gsm3040.ie;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.smslib.UnrecoverableSmslibException;

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
public class ConcatInformationElement extends InformationElement {

    private static final int CONCAT_IE_LENGTH_8BIT = 5;

    public static final int CONCAT_8BIT_REF = 0x00;

    public static final int CONCAT_16BIT_REF = 0x08;

    private static int defaultConcatType = CONCAT_8BIT_REF;

    private static int defaultConcatLength = CONCAT_IE_LENGTH_8BIT;

    public static int getDefaultConcatLength() {
        return defaultConcatLength;
    }

    public static int getDefaultConcatType() {
        return defaultConcatType;
    }

    ConcatInformationElement(byte identifier, byte[] data) {
        super(identifier, data);
        if (getIdentifier() == CONCAT_8BIT_REF) {
            // iei
            // iel
            // ref
            // max
            // seq
            if (data.length != 3) {
                throw new IllegalArgumentException("Invalid data length in: " + getClass().getSimpleName());
            }
        } else if (getIdentifier() == CONCAT_16BIT_REF) {
            // iei
            // iel
            // ref(2 bytes)
            // max
            // seq
            if (data.length != 4) {
                throw new IllegalArgumentException("Invalid data length in: " + getClass().getSimpleName());
            }
        } else {
            throw new IllegalArgumentException("Invalid identifier in data in: " + getClass().getSimpleName());
        }
        validate();
    }

    ConcatInformationElement(int identifier, int mpRefNo, int mpMaxNo, int mpSeqNo) {
        super((byte) (identifier & 0xFF), getData(identifier, mpRefNo, mpMaxNo, mpSeqNo));
        validate();
    }

    private static byte[] getData(int identifier, int mpRefNo, int mpMaxNo, int mpSeqNo) {
        byte[] data = null;
        switch (identifier) {
            case CONCAT_8BIT_REF:
                data = new byte[3];
                data[0] = (byte) (mpRefNo & 0xFF);
                data[1] = (byte) (mpMaxNo & 0xFF);
                data[2] = (byte) (mpSeqNo & 0xFF);
                break;
            case CONCAT_16BIT_REF:
                data = new byte[4];
                data[0] = (byte) ((mpRefNo & 0xFF00) >>> 8);
                data[1] = (byte) (mpRefNo & 0xFF);
                data[2] = (byte) (mpMaxNo & 0xFF);
                data[3] = (byte) (mpSeqNo & 0xFF);
                break;
            default:
                throw new IllegalArgumentException("Invalid identifier for ConcatInformationElement");
        }
        return data;
    }

    public int getMpRefNo() {
        // this is 8-bit in 0x00 and 16-bit in 0x08
        byte[] data = getData();
        if (getIdentifier() == CONCAT_8BIT_REF) {
            return (data[0] & (0xFF));
        } else if (getIdentifier() == CONCAT_16BIT_REF) {
            return ((data[0] << 8) | data[1]) & (0xFFFF);
        }
        throw new UnrecoverableSmslibException("Invalid identifier");
    }

    public void setMpRefNo(int mpRefNo) {
        // this is 8-bit in 0x00 and 16-bit in 0x08
        byte[] data = getData();
        if (getIdentifier() == CONCAT_8BIT_REF) {
            data[0] = (byte) (mpRefNo & (0xFF));
        } else if (getIdentifier() == CONCAT_16BIT_REF) {
            data[0] = (byte) ((mpRefNo >>> 8) & (0xFF));
            data[1] = (byte) ((mpRefNo) & (0xFF));
        } else {
            throw new UnrecoverableSmslibException("Invalid identifier");
        }
    }

    public int getMpMaxNo() {
        byte[] data = getData();
        if (getIdentifier() == CONCAT_8BIT_REF) {
            return (data[1] & (0xFF));
        } else if (getIdentifier() == CONCAT_16BIT_REF) {
            return (data[2] & (0xFF));
        }
        throw new UnrecoverableSmslibException("Invalid identifier");
    }

    public void setMpMaxNo(int mpMaxNo) {
        byte[] data = getData();
        if (getIdentifier() == CONCAT_8BIT_REF) {
            data[1] = (byte) (mpMaxNo & 0xFF);
        } else if (getIdentifier() == CONCAT_16BIT_REF) {
            data[2] = (byte) (mpMaxNo & 0xFF);
        } else {
            throw new UnrecoverableSmslibException("Invalid identifier");
        }
    }

    public int getMpSeqNo() {
        byte[] data = getData();
        if (getIdentifier() == CONCAT_8BIT_REF) {
            return (data[2] & (0xFF));
        } else if (getIdentifier() == CONCAT_16BIT_REF) {
            return (data[3] & (0xFF));
        }
        throw new UnrecoverableSmslibException("Invalid identifier");
    }

    public void setMpSeqNo(int mpSeqNo) {
        byte[] data = getData();
        if (getIdentifier() == CONCAT_8BIT_REF) {
            data[2] = (byte) (mpSeqNo & (0xFF));
        } else if (getIdentifier() == CONCAT_16BIT_REF) {
            data[3] = (byte) (mpSeqNo & (0xFF));
        } else {
            throw new UnrecoverableSmslibException("Invalid identifier");
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("[MpRefNo: ");
        sb.append(getMpRefNo());
        sb.append(", MpMaxNo: ");
        sb.append(getMpMaxNo());
        sb.append(", MpSeqNo: ");
        sb.append(getMpSeqNo());
        sb.append("]");
        return sb.toString();
    }

    private void validate() {
        if (getMpMaxNo() == 0) {
            throw new IllegalArgumentException("mpMaxNo must be > 0");
        }
        if (getMpSeqNo() == 0) {
            throw new IllegalArgumentException("mpSeqNo must be > 0");
        }
    }
}
