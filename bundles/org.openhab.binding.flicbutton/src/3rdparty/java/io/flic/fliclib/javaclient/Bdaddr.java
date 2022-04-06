package io.flic.fliclib.javaclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Bluetooth address.
 */
public class Bdaddr {
    private byte[] bytes;

    /**
     * Creates a Bdaddr given the bluetooth address in string format.
     *
     * @param addr address of the format xx:xx:xx:xx:xx:xx
     */
    public Bdaddr(String addr) {
        bytes = new byte[6];
        bytes[5] = (byte)Integer.parseInt(addr.substring(0, 2), 16);
        bytes[4] = (byte)Integer.parseInt(addr.substring(3, 5), 16);
        bytes[3] = (byte)Integer.parseInt(addr.substring(6, 8), 16);
        bytes[2] = (byte)Integer.parseInt(addr.substring(9, 11), 16);
        bytes[1] = (byte)Integer.parseInt(addr.substring(12, 14), 16);
        bytes[0] = (byte)Integer.parseInt(addr.substring(15, 17), 16);
    }

    Bdaddr(InputStream stream) throws IOException {
        bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte)stream.read();
        }
    }

    byte[] getBytes() {
        return bytes.clone();
    }

    /**
     * Create a string representing the bluetooth address.
     *
     * @return A string of the bdaddr
     */
    @Override
    public String toString() {
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x", bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    }

    @Override
    public int hashCode() {
        return (bytes[0] & 0xff) ^ ((bytes[1] & 0xff) << 8) ^ ((bytes[2] & 0xff) << 16) ^ ((bytes[3] & 0xff) << 24) ^ (bytes[4] & 0xff) ^ ((bytes[5] & 0xff) << 8);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Bdaddr)) {
            return false;
        }
        Bdaddr other = (Bdaddr)obj;
        return Arrays.equals(bytes, other.bytes);
    }
}
