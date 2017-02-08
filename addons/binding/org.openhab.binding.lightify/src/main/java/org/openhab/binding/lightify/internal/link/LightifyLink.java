/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.openhab.binding.lightify.internal.LightifyUtils.exceptional;
import static org.openhab.binding.lightify.internal.link.Command.LIGHT_COLOR;
import static org.openhab.binding.lightify.internal.link.Command.LIGHT_LUMINANCE;
import static org.openhab.binding.lightify.internal.link.Command.LIGHT_SWITCH;
import static org.openhab.binding.lightify.internal.link.Command.LIGHT_TEMPERATURE;
import static org.openhab.binding.lightify.internal.link.Command.STATUS_ALL;
import static org.openhab.binding.lightify.internal.link.Command.STATUS_SINGLE;
import static org.openhab.binding.lightify.internal.link.Command.ZONE_INFO;
import static org.openhab.binding.lightify.internal.link.Command.ZONE_LIST;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class LightifyLink {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightifyLink.class);

    private static final Charset CP437 = Charset.forName("cp437");

    private final AtomicInteger sequencer = new AtomicInteger();
    private final Map<String, LightifyLuminary> devices = new ConcurrentHashMap<>();
    private final Map<String, LightifyZone> zones = new ConcurrentHashMap<>();

    private final String address;
    private final Socket socket;

    private final OutputStream output;
    private final InputStream input;

    public LightifyLink(String address) {
        this.address = address;
        this.socket = exceptional(() -> new Socket(address, 4000));
        this.output = exceptional(socket::getOutputStream);
        this.input = exceptional(socket::getInputStream);
    }

    public LightifyLuminary findDevice(String address) {
        return devices.get(address);
    }

    public LightifyZone findZone(String zoneId) {
        return zones.get(zoneId);
    }

    public void performSearch(Consumer<LightifyLuminary> consumer) {
        byte[] packet = new PacketBuilder(this) //
                                                .on(STATUS_ALL) //
                                                .data(new byte[]{0x01}) //
                                                .build();
        sendPacket(packet);
        readPacket(STATUS_ALL, consumer);

        packet = new PacketBuilder(this) //
                                         .on(ZONE_LIST) //
                                         .build();

        LOGGER.debug("Searching Zones...");
        sendPacket(packet);
        readPacket(ZONE_LIST, consumer);
    }

    public void performStatusUpdate(LightifyLuminary luminary, Consumer<LightifyLuminary> consumer) {
        byte[] packet = new PacketBuilder(this) //
                                                .on(STATUS_SINGLE) //
                                                .with(luminary) //
                                                .build();

        sendPacket(packet);
        readPacket(STATUS_SINGLE, consumer);
    }

    void performSwitch(LightifyLuminary lightifyLuminary, boolean activate, Consumer<LightifyLuminary> consumer) {
        byte[] packet = new PacketBuilder(this) //
                                                .on(LIGHT_SWITCH) //
                                                .with(lightifyLuminary) //
                                                .switching(activate) //
                                                .build();

        sendPacket(packet);
        readPacket(LIGHT_SWITCH, light -> {
            light.updatePowered(activate);
            if (consumer != null) {
                consumer.accept(light);
            }
        });
    }

    void performLuminance(LightifyLuminary lightifyLuminary, byte luminance, //
                          short millis, Consumer<LightifyLuminary> consumer) {

        byte[] packet = new PacketBuilder(this) //
                                                .on(LIGHT_LUMINANCE) //
                                                .with(lightifyLuminary) //
                                                .luminance(luminance) //
                                                .millis(millis) //
                                                .build();

        sendPacket(packet);
        readPacket(LIGHT_LUMINANCE, light -> {
            light.updateLuminance(luminance);
            light.updatePowered(true);
            if (consumer != null) {
                consumer.accept(light);
            }
        });
    }

    void performRGB(LightifyLuminary lightifyLuminary, byte r, byte g, byte b, //
                    short millis, Consumer<LightifyLuminary> consumer) {

        byte[] packet = new PacketBuilder(this) //
                                                .on(LIGHT_COLOR) //
                                                .with(lightifyLuminary) //
                                                .rgb(r, g, b) //
                                                .millis(millis) //
                                                .build();

        sendPacket(packet);
        readPacket(LIGHT_COLOR, light -> {
            light.updateRGB(r, g, b);
            light.updatePowered(true);
            if (consumer != null) {
                consumer.accept(light);
            }
        });
    }

    void performTemperature(LightifyLuminary lightifyLuminary, short temperature, //
                            short millis, Consumer<LightifyLuminary> consumer) {

        byte[] packet = new PacketBuilder(this) //
                                                .on(LIGHT_TEMPERATURE) //
                                                .with(lightifyLuminary) //
                                                .temperature(temperature) //
                                                .millis(millis) //
                                                .build();

        sendPacket(packet);
        readPacket(LIGHT_TEMPERATURE, light -> {
            light.updateTemperature(temperature);
            light.updatePowered(true);
            if (consumer != null) {
                consumer.accept(light);
            }
        });
    }

    void performZoneInfo(LightifyZone lightifyZone, Consumer<LightifyLuminary> consumer) {
        byte[] packet = new PacketBuilder(this) //
                                                .on(ZONE_INFO) //
                                                .with(lightifyZone) //
                                                .build();

        sendPacket(packet);
        readPacket(ZONE_INFO, consumer);
    }

    byte nextSequence() {
        while (true) {
            int oldValue = sequencer.get();
            int next = oldValue + 1;
            if (oldValue > 255) {
                next = 0;
            }
            if (sequencer.compareAndSet(oldValue, next)) {
                return (byte) next;
            }
        }
    }

    private void onPacket(Command command, ByteBuffer buffer, Consumer<LightifyLuminary> consumer) {
        switch (command) {
            case STATUS_ALL:
                handleStatusAll(buffer, consumer);
                break;

            case STATUS_SINGLE:
                handleStatusUpdate(buffer, consumer);
                break;

            case ZONE_LIST:
                handleZoneList(buffer, consumer);
                break;

            case ZONE_INFO:
                handleZoneInfo(buffer, consumer);
                break;

            case LIGHT_SWITCH:
            case LIGHT_LUMINANCE:
            case LIGHT_TEMPERATURE:
            case LIGHT_COLOR:
                handleLightResponse(buffer, command, consumer);
                break;
        }
    }

    private void handleZoneInfo(ByteBuffer buffer, Consumer<LightifyLuminary> consumer) {
        // skip header
        buffer.get();
        if (ZONE_INFO.getId() != buffer.get()) {
            throw new IllegalStateException("Illegal packet type");
        }

        buffer.getShort(); // sequence number
        buffer.get(); // always 0
        buffer.get(); // always 0
        buffer.get(); // always 0

        int zoneId = buffer.getShort();

        byte[] nameBuffer = new byte[16];
        buffer.get(nameBuffer, 0, nameBuffer.length);
        String name = new String(nameBuffer, CP437).trim();

        LightifyZone zone = findZone(getZoneUID(zoneId));

        int numOfDevices = buffer.get();
        for (int i = 0; i < numOfDevices; i++) {
            byte[] address = new byte[8];
            buffer.get(address, 0, address.length);
            String mac = DatatypeConverter.printHexBinary(address);

            LightifyLuminary luminary = findDevice(mac);
            zone.addDevice(luminary);
        }
        consumer.accept(zone);
    }

    private void handleZoneList(ByteBuffer buffer, Consumer<LightifyLuminary> consumer) {
        // skip header
        buffer.get();
        if (ZONE_LIST.getId() != buffer.get()) {
            throw new IllegalStateException("Illegal packet type");
        }

        buffer.getShort(); // sequence number
        buffer.get(); // always 0
        buffer.get(); // always 0
        buffer.get(); // always 0

        int numOfZones = buffer.getShort();
        LOGGER.info("Found {} zones...", numOfZones);
        for (int i = 0; i < numOfZones; i++) {
            int zoneId = buffer.getShort();

            byte[] nameBuffer = new byte[16];
            buffer.get(nameBuffer, 0, nameBuffer.length);
            String name = new String(nameBuffer, CP437).trim();

            LightifyZone zone = new LightifyZone(this, name, zoneId);
            zones.put(getZoneUID(zoneId), zone);
            performZoneInfo(zone, consumer);
        }
    }

    private void handleStatusUpdate(ByteBuffer buffer, Consumer<LightifyLuminary> consumer) {
        // skip header
        buffer.get();
        if (STATUS_SINGLE.getId() != buffer.get()) {
            throw new IllegalStateException("Illegal packet type");
        }

        buffer.getShort(); // sequence number
        buffer.get(); // always 0
        buffer.get(); // always 0
        buffer.get(); // always 0

        int id = buffer.getShort();

        byte[] address = new byte[8];
        buffer.get(address, 0, address.length);
        String mac = DatatypeConverter.printHexBinary(address);
        LightifyLuminary luminary = findDevice(mac);
        if (luminary != null) {
            buffer.getShort(); // unk1

            boolean status = buffer.get() == 1;
            byte luminance = buffer.get();
            short temperature = buffer.getShort();
            byte r = buffer.get();
            byte g = buffer.get();
            byte b = buffer.get();
            byte a = buffer.get();

            luminary.updatePowered(status);
            luminary.updateLuminance(luminance);
            luminary.updateTemperature(temperature);
            luminary.updateRGB(r, g, b);

            consumer.accept(luminary);
        }
    }

    private void handleLightResponse(ByteBuffer buffer, Command command, Consumer consumer) {
        // skip header
        buffer.get();
        if (command.getId() != buffer.get()) {
            throw new IllegalStateException("Illegal packet type");
        }

        buffer.getShort(); // sequence number
        buffer.get(); // always 0
        buffer.get(); // always 0
        buffer.get(); // always 0

        int id = buffer.getShort();

        byte[] address = new byte[8];
        buffer.get(address, 0, address.length);
        String mac = DatatypeConverter.printHexBinary(address);
        LightifyLuminary luminary = findDevice(mac);
        if (luminary != null) {
            consumer.accept(luminary);
        }
    }

    private void handleStatusAll(ByteBuffer buffer, Consumer consumer) {
        // skip header
        buffer.get();
        if (STATUS_ALL.getId() != buffer.get()) {
            throw new IllegalStateException("Illegal packet type");
        }

        buffer.getShort(); // sequence number
        buffer.get(); // always 0
        buffer.get(); // always 0
        buffer.get(); // always 0

        int numOfLights = buffer.getShort();
        LOGGER.debug("Found {} devices...", numOfLights);
        for (int i = 0; i < numOfLights; i++) {
            int id = buffer.getShort();

            // Address (8)
            byte[] address = new byte[8];
            buffer.get(address, 0, address.length);

            // Information (8)
            byte type = buffer.get();
            int firmware = buffer.getInt();
            boolean online = buffer.get() == 1;
            short groupId = buffer.getShort();

            // Stats (8)
            boolean status = buffer.get() == 1;
            byte luminance = buffer.get();
            short temperature = buffer.getShort();
            byte r = buffer.get();
            byte g = buffer.get();
            byte b = buffer.get();
            byte a = buffer.get();
            // Stats

            // Name (24)
            byte[] nameBuffer = new byte[24];
            buffer.get(nameBuffer, 0, nameBuffer.length);
            String name = new String(nameBuffer, CP437).trim();

            LightifyLight light = new LightifyLight(this, name, address);

            // Push values
            light.updateLuminance(luminance);
            light.updateTemperature(temperature);
            light.updateRGB(r, g, b);
            light.updatePowered(status);

            String mac = DatatypeConverter.printHexBinary(light.address());
            devices.put(mac, light);

            consumer.accept(light);
        }
    }

    private void sendPacket(byte[] packet) {
        exceptional(() -> {
            output.write(packet);
            output.flush();
        });
    }

    private void readPacket(Command command, Consumer<LightifyLuminary> consumer) {
        ByteBuffer buffer = exceptional(() -> {
            int length = 2;
            ByteBuffer b = waitForBytes(length, ByteOrder.LITTLE_ENDIAN);

            length = b.getShort();
            return waitForBytes(length, ByteOrder.LITTLE_ENDIAN);
        });

        onPacket(command, buffer, consumer);
    }

    private ByteBuffer waitForBytes(int length, ByteOrder byteOrder)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length).order(byteOrder);
        int read = 0;
        while (read != length) {
            read += input.read(buffer.array(), read, length - read);
        }
        return buffer;
    }

    public void disconnect() {
        exceptional(() -> socket.close());
    }

    private String getZoneUID(int zoneId) {
        return "zone::" + zoneId;
    }
}
