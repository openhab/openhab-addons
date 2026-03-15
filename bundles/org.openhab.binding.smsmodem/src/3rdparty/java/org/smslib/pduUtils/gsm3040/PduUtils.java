package org.smslib.pduUtils.gsm3040;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.smslib.UnrecoverableSmslibException;
import org.smslib.message.MsIsdn;

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
public class PduUtils {
    // ==================================================
    // GSM ALPHABET
    // ==================================================
    private static final char[][] grcAlphabetRemapping = { { '\u0386', '\u0041' }, // GREEK CAPITAL LETTER ALPHA WITH
                                                                                   // TONOS
            { '\u0388', '\u0045' }, // GREEK CAPITAL LETTER EPSILON WITH TONOS
            { '\u0389', '\u0048' }, // GREEK CAPITAL LETTER ETA WITH TONOS
            { '\u038A', '\u0049' }, // GREEK CAPITAL LETTER IOTA WITH TONOS
            { '\u038C', '\u004F' }, // GREEK CAPITAL LETTER OMICRON WITH TONOS
            { '\u038E', '\u0059' }, // GREEK CAPITAL LETTER UPSILON WITH TONOS
            { '\u038F', '\u03A9' }, // GREEK CAPITAL LETTER OMEGA WITH TONOS
            { '\u0390', '\u0049' }, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
            { '\u0391', '\u0041' }, // GREEK CAPITAL LETTER ALPHA
            { '\u0392', '\u0042' }, // GREEK CAPITAL LETTER BETA
            { '\u0393', '\u0393' }, // GREEK CAPITAL LETTER GAMMA
            { '\u0394', '\u0394' }, // GREEK CAPITAL LETTER DELTA
            { '\u0395', '\u0045' }, // GREEK CAPITAL LETTER EPSILON
            { '\u0396', '\u005A' }, // GREEK CAPITAL LETTER ZETA
            { '\u0397', '\u0048' }, // GREEK CAPITAL LETTER ETA
            { '\u0398', '\u0398' }, // GREEK CAPITAL LETTER THETA
            { '\u0399', '\u0049' }, // GREEK CAPITAL LETTER IOTA
            { '\u039A', '\u004B' }, // GREEK CAPITAL LETTER KAPPA
            { '\u039B', '\u039B' }, // GREEK CAPITAL LETTER LAMDA
            { '\u039C', '\u004D' }, // GREEK CAPITAL LETTER MU
            { '\u039D', '\u004E' }, // GREEK CAPITAL LETTER NU
            { '\u039E', '\u039E' }, // GREEK CAPITAL LETTER XI
            { '\u039F', '\u004F' }, // GREEK CAPITAL LETTER OMICRON
            { '\u03A0', '\u03A0' }, // GREEK CAPITAL LETTER PI
            { '\u03A1', '\u0050' }, // GREEK CAPITAL LETTER RHO
            { '\u03A3', '\u03A3' }, // GREEK CAPITAL LETTER SIGMA
            { '\u03A4', '\u0054' }, // GREEK CAPITAL LETTER TAU
            { '\u03A5', '\u0059' }, // GREEK CAPITAL LETTER UPSILON
            { '\u03A6', '\u03A6' }, // GREEK CAPITAL LETTER PHI
            { '\u03A7', '\u0058' }, // GREEK CAPITAL LETTER CHI
            { '\u03A8', '\u03A8' }, // GREEK CAPITAL LETTER PSI
            { '\u03A9', '\u03A9' }, // GREEK CAPITAL LETTER OMEGA
            { '\u03AA', '\u0049' }, // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
            { '\u03AB', '\u0059' }, // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
            { '\u03AC', '\u0041' }, // GREEK SMALL LETTER ALPHA WITH TONOS
            { '\u03AD', '\u0045' }, // GREEK SMALL LETTER EPSILON WITH TONOS
            { '\u03AE', '\u0048' }, // GREEK SMALL LETTER ETA WITH TONOS
            { '\u03AF', '\u0049' }, // GREEK SMALL LETTER IOTA WITH TONOS
            { '\u03B0', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
            { '\u03B1', '\u0041' }, // GREEK SMALL LETTER ALPHA
            { '\u03B2', '\u0042' }, // GREEK SMALL LETTER BETA
            { '\u03B3', '\u0393' }, // GREEK SMALL LETTER GAMMA
            { '\u03B4', '\u0394' }, // GREEK SMALL LETTER DELTA
            { '\u03B5', '\u0045' }, // GREEK SMALL LETTER EPSILON
            { '\u03B6', '\u005A' }, // GREEK SMALL LETTER ZETA
            { '\u03B7', '\u0048' }, // GREEK SMALL LETTER ETA
            { '\u03B8', '\u0398' }, // GREEK SMALL LETTER THETA
            { '\u03B9', '\u0049' }, // GREEK SMALL LETTER IOTA
            { '\u03BA', '\u004B' }, // GREEK SMALL LETTER KAPPA
            { '\u03BB', '\u039B' }, // GREEK SMALL LETTER LAMDA
            { '\u03BC', '\u004D' }, // GREEK SMALL LETTER MU
            { '\u03BD', '\u004E' }, // GREEK SMALL LETTER NU
            { '\u03BE', '\u039E' }, // GREEK SMALL LETTER XI
            { '\u03BF', '\u004F' }, // GREEK SMALL LETTER OMICRON
            { '\u03C0', '\u03A0' }, // GREEK SMALL LETTER PI
            { '\u03C1', '\u0050' }, // GREEK SMALL LETTER RHO
            { '\u03C2', '\u03A3' }, // GREEK SMALL LETTER FINAL SIGMA
            { '\u03C3', '\u03A3' }, // GREEK SMALL LETTER SIGMA
            { '\u03C4', '\u0054' }, // GREEK SMALL LETTER TAU
            { '\u03C5', '\u0059' }, // GREEK SMALL LETTER UPSILON
            { '\u03C6', '\u03A6' }, // GREEK SMALL LETTER PHI
            { '\u03C7', '\u0058' }, // GREEK SMALL LETTER CHI
            { '\u03C8', '\u03A8' }, // GREEK SMALL LETTER PSI
            { '\u03C9', '\u03A9' }, // GREEK SMALL LETTER OMEGA
            { '\u03CA', '\u0049' }, // GREEK SMALL LETTER IOTA WITH DIALYTIKA
            { '\u03CB', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA
            { '\u03CC', '\u004F' }, // GREEK SMALL LETTER OMICRON WITH TONOS
            { '\u03CD', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH TONOS
            { '\u03CE', '\u03A9' } // GREEK SMALL LETTER OMEGA WITH TONOS
    };

    private static final char[] extAlphabet = { '\u000c', // FORM FEED
            '\u005e', // CIRCUMFLEX ACCENT
            '\u007b', // LEFT CURLY BRACKET
            '\u007d', // RIGHT CURLY BRACKET
            '\\', // REVERSE SOLIDUS
            '\u005b', // LEFT SQUARE BRACKET
            '\u007e', // TILDE
            '\u005d', // RIGHT SQUARE BRACKET
            '\u007c', // VERTICAL LINES
            '\u20ac', // EURO SIGN
    };

    private static final String[] extBytes = { "1b0a", // FORM FEED
            "1b14", // CIRCUMFLEX ACCENT
            "1b28", // LEFT CURLY BRACKET
            "1b29", // RIGHT CURLY BRACKET
            "1b2f", // REVERSE SOLIDUS
            "1b3c", // LEFT SQUARE BRACKET
            "1b3d", // TILDE
            "1b3e", // RIGHT SQUARE BRACKET
            "1b40", // VERTICAL LINES
            "1b65", // EURO SIGN
    };

    // NOTE: this is an adjustment required to compensate for
    // multi-byte characters split across the end of a pdu part
    // if the previous part is noted to be ending in a '1b'
    // call this method on the first char of the next part
    // to adjust it for the missing '1b'
    public static String getMultiCharFor(char c) {
        switch (c) {
            // GSM 0x0A (line feed) ==> form feed
            case '\n':
                return "'\u000c'";
            // GSM 0x14 (greek capital lamda) ==> circumflex
            case '\u039B':
                return "^";
            // GSM 0x28 (left parenthesis) ==> left curly brace
            case '(':
                return "{";
            // GSM 0x29 (right parenthesis) ==> right curly brace
            case ')':
                return "}";
            // GSM 0x2f (solidus or slash) ==> reverse solidus or backslash
            case '/':
                return "\\";
            // GSM 0x3c (less than sign) ==> left square bracket
            case '<':
                return "[";
            // GSM 0x3d (equals sign) ==> tilde
            case '=':
                return "~";
            // GSM 0x3e (greater than sign) ==> right square bracket
            case '>':
                return "]";
            // GSM 0x40 (inverted exclamation point) ==> pipe
            case '\u00A1':
                return "|";
            // GSM 0x65 (latin small e) ==> euro
            case 'e':
                return "\u20ac";
        }
        return "";
    }

    private static final char[] stdAlphabet = { '\u0040', // COMMERCIAL AT
            '\u00A3', // POUND SIGN
            '\u0024', // DOLLAR SIGN
            '\u00A5', // YEN SIGN
            '\u00E8', // LATIN SMALL LETTER E WITH GRAVE
            '\u00E9', // LATIN SMALL LETTER E WITH ACUTE
            '\u00F9', // LATIN SMALL LETTER U WITH GRAVE
            '\u00EC', // LATIN SMALL LETTER I WITH GRAVE
            '\u00F2', // LATIN SMALL LETTER O WITH GRAVE
            '\u00E7', // LATIN SMALL LETTER C WITH CEDILLA
            '\n', // LINE FEED
            '\u00D8', // LATIN CAPITAL LETTER O WITH STROKE
            '\u00F8', // LATIN SMALL LETTER O WITH STROKE
            '\r', // CARRIAGE RETURN
            '\u00C5', // LATIN CAPITAL LETTER A WITH RING ABOVE
            '\u00E5', // LATIN SMALL LETTER A WITH RING ABOVE
            '\u0394', // GREEK CAPITAL LETTER DELTA
            '\u005F', // LOW LINE
            '\u03A6', // GREEK CAPITAL LETTER PHI
            '\u0393', // GREEK CAPITAL LETTER GAMMA
            '\u039B', // GREEK CAPITAL LETTER LAMDA
            '\u03A9', // GREEK CAPITAL LETTER OMEGA
            '\u03A0', // GREEK CAPITAL LETTER PI
            '\u03A8', // GREEK CAPITAL LETTER PSI
            '\u03A3', // GREEK CAPITAL LETTER SIGMA
            '\u0398', // GREEK CAPITAL LETTER THETA
            '\u039E', // GREEK CAPITAL LETTER XI
            '\u00A0', // ESCAPE TO EXTENSION TABLE (or displayed as NBSP, see
            // note
            // above)
            '\u00C6', // LATIN CAPITAL LETTER AE
            '\u00E6', // LATIN SMALL LETTER AE
            '\u00DF', // LATIN SMALL LETTER SHARP S (German)
            '\u00C9', // LATIN CAPITAL LETTER E WITH ACUTE
            '\u0020', // SPACE
            '\u0021', // EXCLAMATION MARK
            '\u0022', // QUOTATION MARK
            '\u0023', // NUMBER SIGN
            '\u00A4', // CURRENCY SIGN
            '\u0025', // PERCENT SIGN
            '\u0026', // AMPERSAND
            '\'', // APOSTROPHE
            '\u0028', // LEFT PARENTHESIS
            '\u0029', // RIGHT PARENTHESIS
            '\u002A', // ASTERISK
            '\u002B', // PLUS SIGN
            '\u002C', // COMMA
            '\u002D', // HYPHEN-MINUS
            '\u002E', // FULL STOP
            '\u002F', // SOLIDUS
            '\u0030', // DIGIT ZERO
            '\u0031', // DIGIT ONE
            '\u0032', // DIGIT TWO
            '\u0033', // DIGIT THREE
            '\u0034', // DIGIT FOUR
            '\u0035', // DIGIT FIVE
            '\u0036', // DIGIT SIX
            '\u0037', // DIGIT SEVEN
            '\u0038', // DIGIT EIGHT
            '\u0039', // DIGIT NINE
            '\u003A', // COLON
            '\u003B', // SEMICOLON
            '\u003C', // LESS-THAN SIGN
            '\u003D', // EQUALS SIGN
            '\u003E', // GREATER-THAN SIGN
            '\u003F', // QUESTION MARK
            '\u00A1', // INVERTED EXCLAMATION MARK
            '\u0041', // LATIN CAPITAL LETTER A
            '\u0042', // LATIN CAPITAL LETTER B
            '\u0043', // LATIN CAPITAL LETTER C
            '\u0044', // LATIN CAPITAL LETTER D
            '\u0045', // LATIN CAPITAL LETTER E
            '\u0046', // LATIN CAPITAL LETTER F
            '\u0047', // LATIN CAPITAL LETTER G
            '\u0048', // LATIN CAPITAL LETTER H
            '\u0049', // LATIN CAPITAL LETTER I
            '\u004A', // LATIN CAPITAL LETTER J
            '\u004B', // LATIN CAPITAL LETTER K
            '\u004C', // LATIN CAPITAL LETTER L
            '\u004D', // LATIN CAPITAL LETTER M
            '\u004E', // LATIN CAPITAL LETTER N
            '\u004F', // LATIN CAPITAL LETTER O
            '\u0050', // LATIN CAPITAL LETTER P
            '\u0051', // LATIN CAPITAL LETTER Q
            '\u0052', // LATIN CAPITAL LETTER R
            '\u0053', // LATIN CAPITAL LETTER S
            '\u0054', // LATIN CAPITAL LETTER T
            '\u0055', // LATIN CAPITAL LETTER U
            '\u0056', // LATIN CAPITAL LETTER V
            '\u0057', // LATIN CAPITAL LETTER W
            '\u0058', // LATIN CAPITAL LETTER X
            '\u0059', // LATIN CAPITAL LETTER Y
            '\u005A', // LATIN CAPITAL LETTER Z
            '\u00C4', // LATIN CAPITAL LETTER A WITH DIAERESIS
            '\u00D6', // LATIN CAPITAL LETTER O WITH DIAERESIS
            '\u00D1', // LATIN CAPITAL LETTER N WITH TILDE
            '\u00DC', // LATIN CAPITAL LETTER U WITH DIAERESIS
            '\u00A7', // SECTION SIGN
            '\u00BF', // INVERTED QUESTION MARK
            '\u0061', // LATIN SMALL LETTER A
            '\u0062', // LATIN SMALL LETTER B
            '\u0063', // LATIN SMALL LETTER C
            '\u0064', // LATIN SMALL LETTER D
            '\u0065', // LATIN SMALL LETTER E
            '\u0066', // LATIN SMALL LETTER F
            '\u0067', // LATIN SMALL LETTER G
            '\u0068', // LATIN SMALL LETTER H
            '\u0069', // LATIN SMALL LETTER I
            '\u006A', // LATIN SMALL LETTER J
            '\u006B', // LATIN SMALL LETTER K
            '\u006C', // LATIN SMALL LETTER L
            '\u006D', // LATIN SMALL LETTER M
            '\u006E', // LATIN SMALL LETTER N
            '\u006F', // LATIN SMALL LETTER O
            '\u0070', // LATIN SMALL LETTER P
            '\u0071', // LATIN SMALL LETTER Q
            '\u0072', // LATIN SMALL LETTER R
            '\u0073', // LATIN SMALL LETTER S
            '\u0074', // LATIN SMALL LETTER T
            '\u0075', // LATIN SMALL LETTER U
            '\u0076', // LATIN SMALL LETTER V
            '\u0077', // LATIN SMALL LETTER W
            '\u0078', // LATIN SMALL LETTER X
            '\u0079', // LATIN SMALL LETTER Y
            '\u007A', // LATIN SMALL LETTER Z
            '\u00E4', // LATIN SMALL LETTER A WITH DIAERESIS
            '\u00F6', // LATIN SMALL LETTER O WITH DIAERESIS
            '\u00F1', // LATIN SMALL LETTER N WITH TILDE
            '\u00FC', // LATIN SMALL LETTER U WITH DIAERESIS
            '\u00E0', // LATIN SMALL LETTER A WITH GRAVE
    };

    // ==================================================
    // FIRST OCTET CONSTANTS
    // ==================================================
    // to add, use the & with MASK to clear bits on original value
    // and | this cleared value with constant specified
    // TP-MTI xxxxxx00 = SMS-DELIVER
    // xxxxxx10 = SMS-STATUS-REPORT
    // xxxxxx01 = SMS-SUBMIT
    public static final int TP_MTI_MASK = 0xFC;

    public static final int TP_MTI_SMS_DELIVER = 0x00;

    public static final int TP_MTI_SMS_SUBMIT = 0x01;

    public static final int TP_MTI_SMS_STATUS_REPORT = 0x02;

    // TP-RD xxxxx0xx = accept duplicate messages
    // xxxxx1xx = reject duplicate messages
    // for SMS-SUBMIT only
    public static final int TP_RD_ACCEPT_DUPLICATES = 0x00;

    // TP-VPF xxx00xxx = no validity period
    // xxx10xxx = validity period integer-representation
    // xxx11xxx = validity period timestamp-representation
    public static final int TP_VPF_MASK = 0xE7;

    public static final int TP_VPF_NONE = 0x00;

    public static final int TP_VPF_INTEGER = 0x10;

    public static final int TP_VPF_TIMESTAMP = 0x18;

    // TP-SRI xx0xxxxx = no status report to SME (for SMS-DELIVER only)
    // xx1xxxxx = status report to SME
    public static final int TP_SRI_MASK = 0xDF;

    // TP-SRR xx0xxxxx = no status report (for SMS-SUBMIT only)
    // xx1xxxxx = status report

    public static final int TP_SRR_NO_REPORT = 0x00;

    public static final int TP_SRR_REPORT = 0x20;

    // TP-UDHI x0xxxxxx = no UDH
    // x1xxxxxx = UDH present
    public static final int TP_UDHI_MASK = 0xBF;

    public static final int TP_UDHI_NO_UDH = 0x00;

    public static final int TP_UDHI_WITH_UDH = 0x40;

    // ==================================================
    // ADDRESS-TYPE CONSTANTS
    // ==================================================
    // some typical ones used for sending, though receiving may get other types
    // usually 1 001 0001 (0x91) international format
    // 1 000 0001 (0x81) (unknown) short number (e.g. access codes)
    // 1 101 0000 (0xD0) alphanumeric (e.g. access code names like PasaLoad)
    public static final int ADDRESS_NUMBER_PLAN_ID_TELEPHONE = 0x01;

    public static final int ADDRESS_TYPE_MASK = 0x70;

    public static final int ADDRESS_TYPE_UNKNOWN = 0x00;

    public static final int ADDRESS_TYPE_INTERNATIONAL = 0x10;

    public static final int ADDRESS_TYPE_NATIONAL = 0x20;

    public static final int ADDRESS_TYPE_ALPHANUMERIC = 0x50;

    public static int getAddressTypeFor(MsIsdn number) {
        switch (number.getType()) {
            case International:
                return createAddressType(ADDRESS_TYPE_INTERNATIONAL | ADDRESS_NUMBER_PLAN_ID_TELEPHONE);
            case National:
                return createAddressType(ADDRESS_TYPE_NATIONAL | ADDRESS_NUMBER_PLAN_ID_TELEPHONE);
            default:
                return createAddressType(ADDRESS_TYPE_UNKNOWN | ADDRESS_NUMBER_PLAN_ID_TELEPHONE);
        }
    }

    public static int extractAddressType(int addressType) {
        return addressType & ADDRESS_TYPE_MASK;
    }

    public static int createAddressType(int addressType) {
        // last bit is always set
        return 0x80 | addressType;
    }

    // ==================================================
    // DCS ENCODING CONSTANTS
    // ==================================================
    public static final int DCS_CODING_GROUP_MASK = 0x0F;

    public static final int DCS_CODING_GROUP_DATA = 0xF0;

    public static final int DCS_CODING_GROUP_GENERAL = 0xC0;

    public static final int DCS_ENCODING_MASK = 0xF3;

    public static final int DCS_ENCODING_7BIT = 0x00;

    public static final int DCS_ENCODING_8BIT = 0x04;

    public static final int DCS_ENCODING_UCS2 = 0x08;

    public static final int DCS_MESSAGE_CLASS_MASK = 0xEC;

    public static final int DCS_MESSAGE_CLASS_FLASH = 0x10;

    public static final int DCS_MESSAGE_CLASS_ME = 0x11;

    public static final int DCS_MESSAGE_CLASS_SIM = 0x12;

    public static final int DCS_MESSAGE_CLASS_TE = 0x13;

    public static int extractDcsEncoding(int dataCodingScheme) {
        return dataCodingScheme & ~PduUtils.DCS_ENCODING_MASK;
    }

    public static int extractDcsClass(int dataCodingScheme) {
        return dataCodingScheme & ~DCS_MESSAGE_CLASS_MASK;
    }

    public static int extractDcsFlash(int dataCodingScheme) {
        // this is only useful if DCS != 0
        return dataCodingScheme & ~DCS_MESSAGE_CLASS_MASK;
    }

    public static String decodeDataCodingScheme(Pdu pdu) {
        StringBuffer sb = new StringBuffer();
        switch (PduUtils.extractDcsEncoding(pdu.getDataCodingScheme())) {
            case PduUtils.DCS_ENCODING_7BIT:
                sb.append("7-bit GSM Alphabet");
                break;
            case PduUtils.DCS_ENCODING_8BIT:
                sb.append("8-bit encoding");
                break;
            case PduUtils.DCS_ENCODING_UCS2:
                sb.append("UCS2 encoding");
                break;
        }
        // are flash messages are only applicable to general coding group?
        if ((pdu.getDataCodingScheme() & ~PduUtils.DCS_CODING_GROUP_GENERAL) == 0) {
            switch (PduUtils.extractDcsClass(pdu.getDataCodingScheme())) {
                case PduUtils.DCS_MESSAGE_CLASS_FLASH:
                    sb.append(", (Flash Message)");
                    break;
                case PduUtils.DCS_MESSAGE_CLASS_ME:
                    sb.append(", (Class1 ME Message)");
                    break;
                case PduUtils.DCS_MESSAGE_CLASS_SIM:
                    sb.append(", (Class2 SIM Message)");
                    break;
                case PduUtils.DCS_MESSAGE_CLASS_TE:
                    sb.append(", (Class3 TE Message)");
                    break;
            }
        }
        return sb.toString();
    }

    public static byte[] encode8bitUserData(String text) {
        try {
            return text.getBytes("ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            throw new UnrecoverableSmslibException("Cannot encode user data", e);
        }
    }

    public static byte[] encodeUcs2UserData(String text) {
        try {
            // UTF-16 Big-Endian, no Byte Order Marker at start
            return text.getBytes("UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            throw new UnrecoverableSmslibException("Cannot encode user data", e);
        }
    }

    public static byte[] encode7bitUserData(byte @Nullable [] udhOctets, byte[] textSeptets) {
        // UDH octets and text have to be encoded together in a single pass
        // UDH octets will need to be converted to unencoded septets in order
        // to properly pad the data
        if (udhOctets == null) {
            // convert string to uncompressed septets
            return unencodedSeptetsToEncodedSeptets(textSeptets);
        }
        // convert UDH octets as if they were encoded septets
        // NOTE: DO NOT DISCARD THE LAST SEPTET IF IT IS ZERO
        byte[] udhSeptets = PduUtils.encodedSeptetsToUnencodedSeptets(udhOctets, false);
        // combine the two arrays and encode them as a whole
        byte[] combined = new byte[udhSeptets.length + textSeptets.length];
        System.arraycopy(udhSeptets, 0, combined, 0, udhSeptets.length);
        System.arraycopy(textSeptets, 0, combined, udhSeptets.length, textSeptets.length);
        // convert encoded byte[] to a PDU string
        return unencodedSeptetsToEncodedSeptets(combined);
    }

    public static String decode8bitEncoding(byte @Nullable [] udhData, byte[] pduData) {
        // standard 8-bit characters
        try {
            int udhLength = ((udhData == null) ? 0 : udhData.length);
            return new String(pduData, udhLength, pduData.length - udhLength, "ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            throw new UnrecoverableSmslibException("Cannot decode user data", e);
        }
    }

    public static String decodeUcs2Encoding(byte @Nullable [] udhData, byte[] pduData) {
        try {
            int udhLength = ((udhData == null) ? 0 : udhData.length);
            // standard unicode
            return new String(pduData, udhLength, pduData.length - udhLength, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new UnrecoverableSmslibException("Cannot decode user data", e);
        }
    }

    public static byte swapNibbles(int b) {
        return (byte) (((b << 4) & 0xF0) | ((b >>> 4) & 0x0F));
    }

    public static String readBCDNumbers(int numDigits, byte[] addressData) {
        // reads length BCD numbers from the current position
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < addressData.length; i++) {
            int b = addressData[i];
            int num1 = b & 0x0F;
            sb.append(num1);
            int num2 = (b >>> 4) & 0x0F;
            if (num2 != 0x0F) {
                // check if fillbits
                sb.append(num2);
            }
        }
        return sb.toString();
    }

    public static int createSwappedBCD(int decimal) {
        // creates a swapped BCD representation of a 2-digit decimal
        int tens = (decimal & 0xFF) / 10;
        int ones = (decimal & 0xFF) - (tens * 10);
        return (ones << 4) | tens;
    }

    // from Java String to uncompressed septets (GSM characters)
    public static byte[] stringToUnencodedSeptets(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i, j, index;
        char ch;
        String myS = s;
        myS = myS.replace('\u00C7', // LATIN CAPITAL LETTER C WITH CEDILLA
                '\u00E7' // LATIN SMALL LETTER C WITH CEDILLA
        );
        for (i = 0; i < myS.length(); i++) {
            ch = myS.charAt(i);
            index = -1;
            for (j = 0; j < extAlphabet.length; j++) {
                if (extAlphabet[j] == ch) {
                    index = j;
                    break;
                }
            }
            if (index != -1) // An extended char...
            {
                baos.write((byte) Integer.parseInt(extBytes[index].substring(0, 2), 16));
                baos.write((byte) Integer.parseInt(extBytes[index].substring(2, 4), 16));
            } else
            // Maybe a standard char...
            {
                index = -1;
                for (j = 0; j < stdAlphabet.length; j++) {
                    if (stdAlphabet[j] == ch) {
                        index = j;
                        baos.write((byte) j);
                        break;
                    }
                }
                if (index == -1) // Maybe a Greek Char...
                {
                    for (j = 0; j < grcAlphabetRemapping.length; j++) {
                        if (grcAlphabetRemapping[j][0] == ch) {
                            index = j;
                            ch = grcAlphabetRemapping[j][1];
                            break;
                        }
                    }
                    if (index != -1) {
                        for (j = 0; j < stdAlphabet.length; j++) {
                            if (stdAlphabet[j] == ch) {
                                index = j;
                                baos.write((byte) j);
                                break;
                            }
                        }
                    } else
                    // Unknown char replacement...
                    {
                        baos.write((byte) ' ');
                    }
                }
            }
        }
        return baos.toByteArray();
    }

    // from compress unencoded septets
    public static byte[] unencodedSeptetsToEncodedSeptets(byte[] septetBytes) {
        byte[] txtBytes;
        byte[] txtSeptets;
        int txtBytesLen;
        BitSet bits;
        int i, j;
        txtBytes = septetBytes;
        txtBytesLen = txtBytes.length;
        bits = new BitSet();
        for (i = 0; i < txtBytesLen; i++) {
            for (j = 0; j < 7; j++) {
                if ((txtBytes[i] & (1 << j)) != 0) {
                    bits.set((i * 7) + j);
                }
            }
        }
        // big diff here
        int encodedSeptetByteArrayLength = txtBytesLen * 7 / 8 + ((txtBytesLen * 7 % 8 != 0) ? 1 : 0);
        txtSeptets = new byte[encodedSeptetByteArrayLength];
        for (i = 0; i < encodedSeptetByteArrayLength; i++) {
            for (j = 0; j < 8; j++) {
                txtSeptets[i] |= (byte) ((bits.get((i * 8) + j) ? 1 : 0) << j);
            }
        }
        return txtSeptets;
    }

    // from GSM characters to java string
    public static String unencodedSeptetsToString(byte[] bytes) {
        StringBuffer text;
        String extChar;
        int i, j;
        text = new StringBuffer();
        for (i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0x1b) {
                // NOTE: - ++i can be a problem if the '1b'
                // is right at the end of a PDU
                // - this will be an issue for displaying
                // partial PDUs e.g. via toString()
                if (i < bytes.length - 1) {
                    extChar = "1b" + Integer.toHexString(bytes[++i]);
                    for (j = 0; j < extBytes.length; j++) {
                        if (extBytes[j].equalsIgnoreCase(extChar)) {
                            text.append(extAlphabet[j]);
                        }
                    }
                }
            } else {
                text.append(stdAlphabet[bytes[i]]);
            }
        }
        return text.toString();
    }

    public static int getNumSeptetsForOctets(int numOctets) {
        return numOctets * 8 / 7 + ((numOctets * 8 % 7 != 0) ? 1 : 0);
        // return numOctets + (numOctets/7);
    }

    // decompress encoded septets to unencoded form
    public static byte[] encodedSeptetsToUnencodedSeptets(byte[] octetBytes) {
        return encodedSeptetsToUnencodedSeptets(octetBytes, true);
    }

    public static byte[] encodedSeptetsToUnencodedSeptets(byte[] octetBytes, boolean discardLast) {
        byte newBytes[];
        BitSet bitSet;
        int i, j, value1, value2;
        bitSet = new BitSet(octetBytes.length * 8);
        value1 = 0;
        for (i = 0; i < octetBytes.length; i++) {
            for (j = 0; j < 8; j++) {
                value1 = (i * 8) + j;
                if ((octetBytes[i] & (1 << j)) != 0) {
                    bitSet.set(value1);
                }
            }
        }
        value1++;
        // this is a bit count NOT a byte count
        value2 = value1 / 7 + ((value1 % 7 != 0) ? 1 : 0); // big diff here
        // System.out.println(octetBytes.length);
        // System.out.println(value1+" --> "+value2);
        if (value2 == 0) {
            value2++;
        }
        newBytes = new byte[value2];
        for (i = 0; i < value2; i++) {
            for (j = 0; j < 7; j++) {
                if ((value1 + 1) > (i * 7 + j)) {
                    if (bitSet.get(i * 7 + j)) {
                        newBytes[i] |= (byte) (1 << j);
                    }
                }
            }
        }
        if (discardLast && octetBytes.length * 8 % 7 > 0) {
            // when decoding a 7bit encoded string
            // the last septet may become 0, this should be discarded
            // since this is an artifact of the encoding not part of the
            // original string
            // this is only done for decoding 7bit encoded text NOT for
            // reversing octets to septets (e.g. for the encoding the UDH)
            if (newBytes[newBytes.length - 1] == 0) {
                byte[] retVal = new byte[newBytes.length - 1];
                System.arraycopy(newBytes, 0, retVal, 0, retVal.length);
                return retVal;
            }
        }
        return newBytes;
    }

    // converts a PDU style string to a byte array
    public static byte[] pduToBytes(String s) {
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            bytes[i / 2] = (byte) (Integer.parseInt(s.substring(i, i + 2), 16));
        }
        return bytes;
    }

    // converts a byte array to PDU style string
    public static String bytesToPdu(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(byteToPdu(bytes[i] & 0xFF));
        }
        return sb.toString();
    }

    public static String byteToBits(byte b) {
        String bits = Integer.toBinaryString(b & 0xFF);
        while (bits.length() < 8) {
            bits = "0" + bits;
        }
        return bits;
    }

    public static String byteToPdu(int b) {
        StringBuffer sb = new StringBuffer();
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            sb.append("0");
        }
        sb.append(s);
        return sb.toString().toUpperCase();
    }
}
