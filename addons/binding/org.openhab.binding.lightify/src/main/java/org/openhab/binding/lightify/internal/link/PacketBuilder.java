package org.openhab.binding.lightify.internal.link;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

final class PacketBuilder {

    private final LightifyLink lightifyLink;

    private LightifyLuminary luminary;

    private Command command;

    private byte[] data;

    private Boolean switching;
    private byte[] rgb;
    private Byte luminance;
    private Short temperature;

    private short millis = 0;

    PacketBuilder(LightifyLink lightifyLink) {
        this.lightifyLink = lightifyLink;
    }

    PacketBuilder with(LightifyLuminary luminary) {
        this.luminary = luminary;
        return this;
    }

    PacketBuilder on(Command command) {
        this.command = command;
        return this;
    }

    PacketBuilder switching(boolean switching) {
        this.switching = switching;
        return this;
    }

    PacketBuilder rgb(byte r, byte g, byte b) {
        this.rgb = new byte[]{r, g, b};
        return this;
    }

    PacketBuilder luminance(byte luminance) {
        this.luminance = luminance;
        return this;
    }

    PacketBuilder temperature(short temperature) {
        this.temperature = temperature;
        return this;
    }

    PacketBuilder millis(int millis) {
        this.millis = (short) millis;
        return this;
    }

    PacketBuilder data(byte[] data) {
        this.data = data;
        return this;
    }

    byte[] build() {
        validate();

        int packetSize = calculatePacketSize();
        ByteBuffer buffer = ByteBuffer.allocate(packetSize + 2).order(ByteOrder.LITTLE_ENDIAN);

        short requestId = lightifyLink.nextSequence();
        putHeader(packetSize, requestId, buffer);

        if (luminary != null) {
            putAddressable(buffer);
        } else {
            putGlobal(buffer);
        }
        return buffer.array();
    }

    private void putGlobal(ByteBuffer buffer) {
        if (command == Command.STATUS_ALL) {
            if (data != null) {
                buffer.position(8);
                buffer.put(data);
            }
        }
    }

    private void putAddressable(ByteBuffer buffer) {
        buffer.position(8);
        buffer.put(luminary.address());

        if (command == Command.LIGHT_SWITCH) {
            buffer.put((byte) (switching ? 0x01 : 0x00));
        } else if (command == Command.LIGHT_TEMPERATURE) {
            buffer.putShort(temperature != null ? temperature : 0);
            buffer.putShort(millis);
        } else if (command == Command.LIGHT_LUMINANCE) {
            buffer.put(luminance != null ? luminance : 0);
            buffer.putShort(millis);
        } else if (command == Command.LIGHT_COLOR) {
            byte r = rgb != null ? rgb[0] : 0;
            byte g = rgb != null ? rgb[1] : 0;
            byte b = rgb != null ? rgb[2] : 0;
            buffer.put(r);
            buffer.put(g);
            buffer.put(b);
            buffer.put((byte) 0xff);
            buffer.putShort(millis);
        }
    }

    private void putHeader(int packetSize, short requestId, ByteBuffer buffer) {
        buffer.putShort(0, (short) packetSize);

        byte zoneOrNode = command.isZone() ? 0x02 : luminary.typeFlag();
        buffer.put(2, zoneOrNode);

        buffer.put(3, command.getId());
        buffer.putShort(4, requestId);
    }

    private void validate() {
        Objects.requireNonNull(command, "command must be set");

        if (!command.isZone()) {
            if (luminary == null) {
                throw new NullPointerException("luminary must be set for non global commands");
            }
        }
        if (command == Command.LIGHT_COLOR && rgb == null) {
            throw new NullPointerException("rgb not set for rgb command");
        }
        if (command == Command.LIGHT_LUMINANCE && luminance == null) {
            throw new NullPointerException("luminance not set for luminance command");
        }
        if (command == Command.LIGHT_TEMPERATURE && temperature == null) {
            throw new NullPointerException("temperature not set for temperature command");
        }
    }

    private int calculatePacketSize() {
        int size = command.isZone() ? 6 : 14; // header
        if (luminary != null) {
            size += 8; // address bytes
        }
        if (switching != null) {
            size += 1;
        }
        if (rgb != null) {
            size += 6;
        }
        if (luminance != null) {
            size += 3;
        }
        if (temperature != null) {
            size += 4;
        }
        if (data != null) {
            size += data.length;
        }
        return size;
    }
}
