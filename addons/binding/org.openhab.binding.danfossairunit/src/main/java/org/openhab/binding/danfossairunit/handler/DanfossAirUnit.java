package org.openhab.binding.danfossairunit.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DanfossAirUnit {

    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnit.class);

    private InetAddress inetAddr;
    private int port;
    private Socket socket;
    private OutputStream oStream;
    private InputStream iStream;
    public static final byte MODE_AUTOMATIC = 1;
    public static final byte MODE_MANUAL = 2;
    public static final byte MODE_PROGRAM = 3;
    private static final byte[] DISCOVER_SEND = { 0x0c, 0x00, 0x30, 0x00, 0x11, 0x00, 0x12, 0x00, 0x13 };
    private static final byte[] DISCOVER_RECEIVE = { 0x0d, 0x00, 0x07, 0x00, 0x02, 0x02, 0x00 };
    private static final byte[] EMPTY = {};
    private static final byte[] GET_HISTORY = { 0x00, 0x30 };
    private static final byte[] REGISTER_1_READ = { 0x01, 0x04 };
    private static final byte[] REGISTER_1_WRITE = { 0x01, 0x06 };
    private static final byte[] REGISTER_4_READ = { 0x04, 0x04 };
    private static final byte[] REGISTER_6_READ = { 0x06, 0x04 };
    private static final byte[] MODE = { 0x14, 0x12 };
    private static final byte[] FAN_SPEED = { 0x15, 0x61 };
    private static final byte[] BASE_IN = { 0x14, 0x40 };
    private static final byte[] BASE_OUT = { 0x14, 0x41 };
    private static final byte[] BYPASS = { 0x14, 0x60 };
    private static final byte[] BYPASS_DEACTIVATION = { 0x14, 0x63 };
    private static final byte[] BOOST = { 0x15, 0x30 };
    private static final byte[] NIGHT_COOLING = { 0x15, 0x71 };
    private static final byte[] AUTOMATIC_BYPASS = { 0x17, 0x06 };
    private static final byte[] AUTOMATIC_RUSH_AIRING = { 0x17, 0x02 };
    private static final byte[] MODEL = { 0x00, 0x25 };
    private static final byte[] HUMIDITY = { 0x14, 0x70 };
    private static final byte[] ROOM_TEMPERATURE = { 0x03, 0x00 };
    private static final byte[] OUTDOOR_TEMPERATURE = { 0x03, 0x34 };
    private static final byte[] SUPPLY_TEMPERATURE = { 0x14, 0x73 };
    private static final byte[] EXTRACT_TEMPERATURE = { 0x14, 0x74 };
    private static final byte[] EXHAUST_TEMPERATURE = { 0x14, 0x75 };
    private static final byte[] BATTERY_LIFE = { 0x03, 0x0f };
    private static final byte[] FILTER_LIFE = { 0x14, 0x6a };
    private static final byte[] CURRENT_TIME = { 0x15, (byte) 0xe0 };
    private static final byte[] AWAY_TO = { 0x15, (byte) 0x20 };
    private static final byte[] AWAY_FROM = { 0x15, (byte) 0x21 };

    public DanfossAirUnit(InetAddress inetAddr, int port) throws IOException {
        this.inetAddr = inetAddr;
        this.port = port;
        connect();
    }

    private void connect() throws IOException {
        socket = new Socket(inetAddr, port);
        oStream = socket.getOutputStream();
        iStream = socket.getInputStream();
    }

    public void disconnect() {
        try {
            socket.close();

        } catch (IOException ioe) {
            // TODO: handle exception
        }
    }

    private byte[] sendRobustRequest(byte[] operation, byte[] register) throws IOException {
        return sendRobustRequest(operation, register, EMPTY);
    }

    private synchronized byte[] sendRobustRequest(byte[] operation, byte[] register, byte[] value) throws IOException {
        byte[] request = new byte[4 + value.length];
        System.arraycopy(operation, 0, request, 0, 2);
        System.arraycopy(register, 0, request, 2, 2);
        System.arraycopy(value, 0, request, 4, value.length);

        try {
            return sendRequest(request);

        } catch (IOException exIo) {
            connect();
            return sendRequest(request);
        }
    }

    private byte[] sendRequest(byte[] request) throws IOException {

        oStream.write(request);
        oStream.flush();

        byte result[] = new byte[63];
        iStream.read(result, 0, 63);

        return result;
    }

    private boolean getBoolean(byte[] operation, byte[] register) throws IOException {
        return sendRobustRequest(operation, register)[0] != 0;
    }

    private void setSetting(byte[] register, boolean value) throws IOException {
        setSetting(register, value ? (byte) 1 : (byte) 0);
    }

    private byte getByte(byte[] operation, byte[] register) throws IOException {
        return sendRobustRequest(operation, register)[0];
    }

    private void setSetting(byte[] register, byte value) throws IOException {
        byte[] valueArray = { value };
        sendRobustRequest(REGISTER_1_WRITE, register, valueArray);
    }

    private short getShort(byte[] operation, byte[] register) throws IOException {
        byte[] result = sendRobustRequest(operation, register);
        return (short) ((result[0] << 8) + (result[1] & 0xff));
    }

    private float getTemperature(byte[] operation, byte[] register) throws IOException {
        return ((float) getShort(operation, register)) / 100;
    }

    private Date getTimestamp(byte[] operation, byte[] register) throws IOException {
        byte[] result = sendRobustRequest(operation, register);
        return asDate(result);
    }

    private static Date asDate(byte[] data) {
        java.util.Calendar cal = Calendar.getInstance();

        int second = data[0];
        int minute = data[1];
        int hour = data[2] & 0x1f;
        int day = data[3] & 0x1f;
        int month = data[4] - 1;
        int year = data[5] + 2000;
        cal.set(year, month, day, hour, minute, second);
        return cal.getTime();
    }

    private static int asUnsignedByte(byte b) {
        return b & 0xFF;
    }

    private static float asPercentByte(byte b) {
        float f = asUnsignedByte(b);
        return f * 100 / 255;
    }

    private void setSetting(byte[] register, short value) throws IOException {
        byte[] valueArray = new byte[2];
        valueArray[0] = (byte) (value >> 8);
        valueArray[1] = (byte) value;

        sendRobustRequest(REGISTER_1_WRITE, register, valueArray);
    }

    public StringType getMode() throws IOException {
        return new StringType(new Integer(getByte(REGISTER_1_READ, MODE)).toString());
    }

    public DecimalType getFanSpeed() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getByte(REGISTER_1_READ, FAN_SPEED)));
    }

    public OnOffType getBoost() throws IOException {
        return getBoolean(REGISTER_1_READ, BOOST) ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getBypass() throws IOException {
        return getBoolean(REGISTER_1_READ, BYPASS) ? OnOffType.ON : OnOffType.OFF;
    }

    public DecimalType getHumidity() throws IOException {
        return new DecimalType(BigDecimal.valueOf(asPercentByte(getByte(REGISTER_1_READ, HUMIDITY))));
    }

    public DecimalType getRoomTemperature() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getTemperature(REGISTER_1_READ, ROOM_TEMPERATURE)));
    }

    public DecimalType getOutdoorTemperature() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getTemperature(REGISTER_1_READ, OUTDOOR_TEMPERATURE)));
    }

    public DecimalType getSupplyTemperature() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getTemperature(REGISTER_4_READ, SUPPLY_TEMPERATURE)));
    }

    public DecimalType getExtractTemperature() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getTemperature(REGISTER_4_READ, EXTRACT_TEMPERATURE)));
    }

    public DecimalType getExhaustTemperature() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getTemperature(REGISTER_4_READ, EXHAUST_TEMPERATURE)));
    }

    public DecimalType getBatteryLife() throws IOException {
        return new DecimalType(BigDecimal.valueOf(asUnsignedByte(getByte(REGISTER_1_READ, BATTERY_LIFE))));
    }

    public DecimalType getFilterLife() throws IOException {
        return new DecimalType(BigDecimal.valueOf(asPercentByte(getByte(REGISTER_1_READ, FILTER_LIFE))));
    }

}
