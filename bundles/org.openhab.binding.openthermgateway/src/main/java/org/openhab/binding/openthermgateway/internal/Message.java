package org.openhab.binding.openthermgateway.internal;

public class Message {
    /*
     * The code field is not part of OpenTherm specification, but added by OpenTherm Gateway.
     * It can be any of the following:
     *
     * T: Message received from the thermostat
     * B: Message received from the boiler
     * R: Request sent to the boiler
     * A: Response returned to the thermostat
     * E: Parity or stop bit error
     */

    private String code;
    private MessageType messageType;
    private int id;
    private String data;

    public String getCode() {
        return this.code;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getID() {
        return id;
    }

    public String getData(ByteType byteType) {
        if (this.data != null && this.data.length() == 4) {
            switch (byteType) {
                case HighByte:
                    return this.data.substring(0, 2);
                case LowByte:
                    return this.data.substring(2, 4);
                case Both:
                    return this.data;
            }
        }

        return null;
    }

    public boolean getBit(ByteType byteType, int pos) {
        String data = getData(byteType);
        if (data != null) {
            // First parse the hex value to an integer
            int parsed = Integer.parseInt(data, 16);

            // Then right shift it pos positions so that the required bit is at the front
            // and then apply a bitmask of 00000001 (1)
            return ((parsed >> pos) & 1) == 1;
        }

        return false;
    }

    public int getUInt(ByteType byteType) {
        String data = getData(byteType);
        return Integer.parseInt(data, 16);
    }

    public int getInt(ByteType byteType) {
        String data = getData(byteType);

        if (data != null) {
            return parseSignedInteger(data);
        }

        return 0;
    }

    public float getFloat() {
        // f8.8, two's complement

        String data = getData(ByteType.Both);

        long value = Long.parseLong(data, 16);

        // left padded with zeros
        String binary = String.format("%16s", Long.toBinaryString(value)).replace(' ', '0');

        if (binary.charAt(0) == '1') {
            // negative value

            String inverted = invertBinary(binary);

            value = Long.parseLong(inverted, 2);
            value = value + 1;
            value = value * -1;
        }

        // divide by 2^8 = 256
        return (float) value / 256;
    }

    public boolean overrides(Message other) {
        // If the message is a Request sent to the boiler or an Answer returned to the
        // thermostat, and it's ID is equal to the previous message, then this is an
        // override sent by the OpenTherm Gateway
        if (other != null && this.getID() == other.getID() && (this.getCode() == "R" || this.getCode() == "A")) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", this.code, this.id, this.data);
    }

    public Message(String code, MessageType messageType, int id, String data) {
        this.code = code;
        this.messageType = messageType;
        this.id = id;
        this.data = data;
    }

    public static Message parse(String message) {
        if (message != null && message.matches("[TBRA]{1}[A-F0-9]{8}")) {

            // For now, only parse TBRA codes
            String code = message.substring(0, 1);
            MessageType messageType = getMessageType(message.substring(1, 3));
            int id = Integer.valueOf(message.substring(3, 5), 16);
            String data = message.substring(5);

            return new Message(code, messageType, id, data);
        }

        return null;
    }

    private static MessageType getMessageType(String value) {
        // First parse the hex value to an integer
        int integer = Integer.parseInt(value, 16);

        // Then right shift it 4 bits so that the message type bits are at the front
        int shifted = integer >> 4;

        // Then mask it with 00000111 (7), so that we only get the first 3 bits,
        // effectively cutting off the parity bit.
        int cutoff = shifted & 7;

        switch (cutoff) {
            case 0: // 000
                return MessageType.ReadData;
            case 1: // 001
                return MessageType.WriteData;
            case 2: // 010
                return MessageType.InvalidData;
            case 3: // 011
                return MessageType.Reserved;
            case 4: // 100
                return MessageType.ReadAck;
            case 5: // 101
                return MessageType.WriteAck;
            case 6: // 110
                return MessageType.DataInvalid;
            case 7: // 111
            default:
                return MessageType.UnknownDataId;
        }
    }

    private int parseSignedInteger(String data) {
        // First parse the hex value to an unsigned integer value
        int result = Integer.parseInt(data, 16);

        // TODO: more elegant way of determining the bitmask based on the parsed integer
        // value

        if (data.length() == 4) {
            // This is a two byte value, apply a bitmask of 01111111 11111111 (32767) to cut
            // off the sign bit
            result = result & 32767;

            // Then apply a bitmask of 10000000 00000000 (32768) to check the sign bit
            if ((result & 32768) == 32768) {
                // If the sign is 1000000 00000000 (32768) then it's a negative
                result = -32768 + result;
            }
        } else {
            // This is a one byte value, apply a bitmask of 01111111 (127), to cut off the
            // sign bit
            result = result & 127;

            // Then apply a bitmask of 10000000 (128) to check the sign bit
            if ((result & 128) == 128) {
                // If the sign is 1000000 (128) then it's a negative
                result = -128 + result;
            }
        }

        return result;
    }

    private String invertBinary(String value) {
        // There is probably a better solution, but for now this works
        String result = value;

        result = result.replace('1', 'X');
        result = result.replace('0', '1');
        result = result.replace('X', '0');

        return result;
    }
}