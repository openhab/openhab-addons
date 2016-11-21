package org.openhab.binding.ivtheatpump.internal.protocol;

class Checksum {
    static byte calculate(byte[]... lists) {
        byte checksum = 0;

        for (byte[] list : lists) {
            for (byte b : list) {
                checksum = (byte) (checksum ^ b);
            }
        }

        return checksum;
    }
}
