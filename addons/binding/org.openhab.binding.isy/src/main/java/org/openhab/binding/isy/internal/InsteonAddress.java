package org.openhab.binding.isy.internal;

public class InsteonAddress {

    String mByte1;
    String mByte2;
    String mByte3;
    int mDeviceId;
    private static int UNSPECIFIED_DEVICE_ID = 1243345;

    public InsteonAddress(String address, int deviceId) {
        String[] addressParts = address.split(" ");
        mByte1 = addressParts[0];
        mByte2 = addressParts[1];
        mByte3 = addressParts[2];
        mDeviceId = deviceId;
    }

    public InsteonAddress(String address) {
        String[] addressParts = address.split(" ");
        mByte1 = addressParts[0];
        mByte2 = addressParts[1];
        mByte3 = addressParts[2];
        int deviceId = Integer.parseInt(addressParts[3]);
        if (deviceId > 0) {
            mDeviceId = deviceId;
        } else {
            mDeviceId = UNSPECIFIED_DEVICE_ID;
        }
    }

    public InsteonAddress(String byte1, String byte2, String byte3, int deviceId) {
        mByte1 = byte1;
        mByte2 = byte2;
        mByte3 = byte3;
        mDeviceId = deviceId;
    }

    public String toStringNoDeviceId() {
        return new StringBuilder().append(mByte1).append(" ").append(mByte2).append(" ").append(mByte3).toString();
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public boolean matchesExcludingDeviceId(String address) {
        String[] addressParts = address.split(" ");
        return mByte1.equals(addressParts[0]) && mByte2.equals(addressParts[1]) && mByte3.equals(addressParts[2]);
    }

    public boolean matchesExcludingDeviceId(InsteonAddress address) {
        return address.mByte1.equals(mByte1) && address.mByte2.equals(mByte2) && address.mByte3.equals(mByte3);
    }

    public static String stripDeviceId(String insteonAddress) {
        String[] addressParts = insteonAddress.split(" ");
        return new StringBuilder().append(addressParts[0]).append(" ").append(addressParts[1]).append(" ")
                .append(addressParts[2]).toString();
    }

    // TODO implement hashCode?
    private String pad(String theByte) {
        if (theByte.length() == 1) {
            return "0" + theByte;
        } else {
            return theByte;
        }
    }

    public String toStringPaddedBytes() {
        return new StringBuilder().append(pad(mByte1)).append(" ").append(pad(mByte2)).append(" ").append(pad(mByte3))
                .append(" ").append(mDeviceId).toString();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(mByte1).append(" ").append(mByte2).append(" ").append(mByte3).append(" ")
                .append(mDeviceId).toString();
    }
}
