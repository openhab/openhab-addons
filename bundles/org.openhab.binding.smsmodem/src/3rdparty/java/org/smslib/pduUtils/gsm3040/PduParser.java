package org.smslib.pduUtils.gsm3040;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.smslib.UnrecoverableSmslibException;
import org.smslib.message.MsIsdn;
import org.smslib.pduUtils.gsm3040.ie.InformationElement;
import org.smslib.pduUtils.gsm3040.ie.InformationElementFactory;

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
public class PduParser {
    // ==================================================
    // RAW PDU PARSER
    // ==================================================
    // increments as methods are called
    private int position;

    private byte @Nullable [] pduByteArray;

    // possible types of data
    // BCD digits
    // byte
    // gsm-septets
    // timestamp info
    private int readByte() {
        // read 8-bits forward
        byte[] pduByteArrayFinal = this.pduByteArray;
        if (pduByteArrayFinal == null) {
            throw new UnrecoverableSmslibException("Cannot read byte from null data");
        }
        int retVal = pduByteArrayFinal[this.position] & 0xFF;
        this.position++;
        return retVal;
    }

    private int readSwappedNibbleBCDByte() {
        // read 8-bits forward, swap the nibbles
        int data = readByte();
        data = PduUtils.swapNibbles((byte) data);
        int retVal = 0;
        retVal += ((data >>> 4) & 0xF) * 10;
        retVal += ((data & 0xF));
        return retVal;
    }

    private Calendar readTimeStamp() {
        // reads timestamp info
        // 7 bytes in semi-octet(BCD) style
        int year = readSwappedNibbleBCDByte();
        int month = readSwappedNibbleBCDByte();
        int day = readSwappedNibbleBCDByte();
        int hour = readSwappedNibbleBCDByte();
        int minute = readSwappedNibbleBCDByte();
        int second = readSwappedNibbleBCDByte();
        // special treatment for timezone due to sign bit
        // swap nibbles, clear the sign bit, convert remaining bits to BCD
        int timestamp = readByte();
        boolean negative = (timestamp & 0x08) == 0x08; // check bit 3
        int timezone = PduUtils.swapNibbles(timestamp) & 0x7F; // remove last bit since this is just a sign
        // time zone computation
        TimeZone tz = null;
        if (negative) {
            // bit 3 of unswapped value represents the sign (1 == negative, 0 == positive)
            // when swapped this will now be bit 7 (128)
            int bcdTimeZone = 0;
            bcdTimeZone += (((timezone >>> 4) & 0xF) * 10);
            bcdTimeZone += ((timezone & 0xF));
            timezone = bcdTimeZone;
            int totalMinutes = timezone * 15;
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            String gmtString = "GMT-" + hours + ":" + (minutes < 10 ? "0" : "") + minutes;
            // System.out.println(gmtString);
            tz = TimeZone.getTimeZone(gmtString);
        } else {
            int bcdTimeZone = 0;
            bcdTimeZone += ((timezone >>> 4) & 0xF) * 10;
            bcdTimeZone += ((timezone & 0xF));
            timezone = bcdTimeZone;
            int totalMinutes = timezone * 15;
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            String gmtString = "GMT+" + hours + ":" + (minutes < 10 ? "0" : "") + minutes;
            // System.out.println(gmtString);
            tz = TimeZone.getTimeZone(gmtString);
        }
        Calendar cal = Calendar.getInstance(tz);
        cal.set(Calendar.YEAR, year + 2000);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        return cal;
    }

    private @Nullable String readAddress(int addressLength, int addressType) {
        // NOTE: the max number of octets on an address is 12 octets
        // this means that an address field need only be 12 octets long
        // what about for 7-bit? This would be 13 chars at 12 octets?
        if (addressLength > 0) {
            // length is a semi-octet count
            int addressDataOctetLength = addressLength / 2 + ((addressLength % 2 == 1) ? 1 : 0);
            // extract data and increment position
            byte[] addressData = new byte[addressDataOctetLength];
            byte[] pduByteArrayFinal = this.pduByteArray;
            if (pduByteArrayFinal != null) {
                System.arraycopy(pduByteArrayFinal, this.position, addressData, 0, addressDataOctetLength);
            } else {
                throw new UnrecoverableSmslibException("Cannot read address because pdu data is null");
            }
            this.position = this.position + addressDataOctetLength;
            switch (PduUtils.extractAddressType(addressType)) {
                case PduUtils.ADDRESS_TYPE_ALPHANUMERIC:
                    // extract and process encoded bytes
                    byte[] uncompressed = PduUtils.encodedSeptetsToUnencodedSeptets(addressData);
                    int septets = addressLength * 4 / 7;
                    byte[] choppedAddressData = new byte[septets];
                    System.arraycopy(uncompressed, 0, choppedAddressData, 0, septets);
                    return PduUtils.unencodedSeptetsToString(choppedAddressData);
                default:
                    // process BCD style data any other
                    return PduUtils.readBCDNumbers(addressLength, addressData);
            }
        }
        return null;
    }

    private int readValidityPeriodInt() {
        // this will convert the VP to #MINUTES
        int validity = readByte();
        int minutes = 0;
        if ((validity > 0) && (validity <= 143)) {
            // groups of 5 min
            minutes = (validity + 1) * 5;
        } else if ((validity > 143) && (validity <= 167)) {
            // groups of 30 min + 12 hrs
            minutes = (12 * 60) + (validity - 143) * 30;
        } else if ((validity > 167) && (validity <= 196)) {
            // days
            minutes = (validity - 166) * 24 * 60;
        } else if ((validity > 197) && (validity <= 255)) {
            // weeks
            minutes = (validity - 192) * 7 * 24 * 60;
        }
        return minutes;
    }

    public Pdu parsePdu(String rawPdu) {
        // encode pdu to byte[] for easier processing
        this.pduByteArray = PduUtils.pduToBytes(rawPdu);
        this.position = 0;
        // parse start and determine what type of pdu it is
        Pdu pdu = parseStart();
        // parse depending on the pdu type
        switch (pdu.getTpMti()) {
            case PduUtils.TP_MTI_SMS_DELIVER:
                parseSmsDeliverMessage((SmsDeliveryPdu) pdu);
                break;
            case PduUtils.TP_MTI_SMS_SUBMIT:
                parseSmsSubmitMessage((SmsSubmitPdu) pdu);
                break;
            case PduUtils.TP_MTI_SMS_STATUS_REPORT:
                parseSmsStatusReportMessage((SmsStatusReportPdu) pdu);
                break;
        }
        return pdu;
    }

    private Pdu parseStart() {
        // SMSC info
        // length
        // address type
        // smsc data
        int addressLength = readByte();
        Pdu pdu = null;
        if (addressLength > 0) {
            int addressType = readByte();
            String smscAddress = readAddress((addressLength - 1) * 2, addressType);
            // first octet - determine how to parse and how to store
            int firstOctet = readByte();
            pdu = PduFactory.createPdu(firstOctet);
            // generic methods
            pdu.setSmscAddressType(addressType);
            pdu.setSmscAddress(smscAddress);
            pdu.setSmscInfoLength(addressLength);
        } else {
            // first octet - determine how to parse and how to store
            int firstOctet = readByte();
            pdu = PduFactory.createPdu(firstOctet);
        }
        return pdu;
    }

    private void parseUserData(Pdu pdu) {
        // ud length
        // NOTE: - the udLength value is just stored, it is not used to determine the length
        // of the remaining data (it may be a septet length not an octet length)
        // - parser just assumes that the remaining PDU data is for the User-Data field
        int udLength = readByte();
        pdu.setUDLength(udLength);
        // user data
        // NOTE: UD Data does not contain the length octet
        byte[] pduByteArrayFinal = this.pduByteArray;
        if (pduByteArrayFinal != null) {
            int udOctetLength = pduByteArrayFinal.length - this.position;
            byte[] udData = new byte[udOctetLength];
            System.arraycopy(pduByteArrayFinal, this.position, udData, 0, udOctetLength);
            // save the UD data
            pdu.setUDData(udData);
        } else {
            throw new UnrecoverableSmslibException("Cannot parse user data because pdu data is null");
        }
        // user data header (if present)
        // position is still at the start of the UD
        if (pdu.hasTpUdhi()) {
            // udh length
            int udhLength = readByte();
            // udh data (iterate till udh is consumed)
            // iei id
            // iei data length
            // iei data
            int endUdh = this.position + udhLength;
            while (this.position < endUdh) {
                int iei = readByte();
                int iedl = readByte();
                byte[] ieData = new byte[iedl];
                System.arraycopy(pduByteArrayFinal, this.position, ieData, 0, iedl);
                InformationElement ie = InformationElementFactory.createInformationElement(iei, ieData);
                pdu.addInformationElement(ie);
                this.position = this.position + iedl;
                if (this.position > endUdh) {
                    // at the end, position after adding should be exactly at endUdh
                    throw new UnrecoverableSmslibException(
                            "UDH is shorter than expected endUdh=" + endUdh + ", position=" + this.position);
                }
            }
        }
    }

    private void parseSmsDeliverMessage(SmsDeliveryPdu pdu) {
        // originator address info
        // address length
        // type of address
        // address data
        int addressLength = readByte();
        int addressType = readByte();
        String originatorAddress = readAddress(addressLength, addressType);
        pdu.setAddressType(addressType);
        if (originatorAddress != null) {
            pdu.setAddress(new MsIsdn(originatorAddress));
        }
        // protocol id
        int protocolId = readByte();
        pdu.setProtocolIdentifier(protocolId);
        // data coding scheme
        int dcs = readByte();
        pdu.setDataCodingScheme(dcs);
        // timestamp
        Calendar timestamp = readTimeStamp();
        pdu.setTimestamp(timestamp);
        // user data
        parseUserData(pdu);
    }

    private void parseSmsStatusReportMessage(SmsStatusReportPdu pdu) {
        // message reference
        int messageReference = readByte();
        pdu.setMessageReference(messageReference);
        // destination address info
        int addressLength = readByte();
        int addressType = readByte();
        String destinationAddress = readAddress(addressLength, addressType);
        pdu.setAddressType(addressType);
        pdu.setAddress(new MsIsdn(destinationAddress));
        // timestamp
        Calendar timestamp = readTimeStamp();
        pdu.setTimestamp(timestamp);
        // discharge time(timestamp)
        Calendar timestamp2 = readTimeStamp();
        pdu.setDischargeTime(timestamp2);
        // status
        int status = readByte();
        pdu.setStatus(status);
    }

    // NOTE: the following is just for validation of the PduGenerator
    // - there is no normal scenario where this is used
    private void parseSmsSubmitMessage(SmsSubmitPdu pdu) {
        // message reference
        int messageReference = readByte();
        pdu.setMessageReference(messageReference);
        // destination address info
        int addressLength = readByte();
        int addressType = readByte();
        String destinationAddress = readAddress(addressLength, addressType);
        pdu.setAddressType(addressType);
        pdu.setAddress(new MsIsdn(destinationAddress));
        // protocol id
        int protocolId = readByte();
        pdu.setProtocolIdentifier(protocolId);
        // data coding scheme
        int dcs = readByte();
        pdu.setDataCodingScheme(dcs);
        // validity period
        switch (pdu.getTpVpf()) {
            case PduUtils.TP_VPF_NONE:
                break;
            case PduUtils.TP_VPF_INTEGER:
                int validityInt = readValidityPeriodInt();
                pdu.setValidityPeriod(validityInt / 60); // pdu assumes hours
                break;
            case PduUtils.TP_VPF_TIMESTAMP:
                Calendar validityDate = readTimeStamp();
                pdu.setValidityTimestamp(validityDate);
                break;
        }
        parseUserData(pdu);
    }
}
