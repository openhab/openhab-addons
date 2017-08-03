package org.openhab.binding.bluetoothsmart.internal;

import java.util.Arrays;
import java.util.List;

import org.openhab.binding.bluetoothsmart.BluetoothSmartBindingConstants;
import org.sputnikdev.bluetooth.URL;

public class BluetoothSmartUtils {

    private static final String MAC_PART_REGEXP = "(\\w{2}(?=(\\w{2})))";

    private BluetoothSmartUtils() {}

    public static String getThingUID(URL url) {
        return url.toString().replaceAll("/", "").replaceAll(":", "");
    }

    public static URL getURL(String thingUID) {
        String adapterAddress = thingUID.substring(0, 12).replaceAll(MAC_PART_REGEXP, "$1:");
        String deviceAddress = null;
        if (thingUID.length() > 12 && thingUID.length() <= 24) {
            deviceAddress = thingUID.substring(12, 24).replaceAll(MAC_PART_REGEXP, "$1:");
        }
        return new URL(adapterAddress, deviceAddress);
    }

    public static String getShortUUID(String longUUID) {
        if (longUUID.length() < 8) {
            return longUUID;
        }
        return Long.toHexString(Long.valueOf(longUUID.substring(0, 8), 16)).toUpperCase();
    }

    public static String getChannelUID(URL url) {
        String channelUID = (getShortUUID(url.getServiceUUID()) +
                "-" + getShortUUID(url.getCharacteristicUUID()));

        if (url.getFieldName() != null) {
            channelUID += "-" + url.getFieldName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }

        return channelUID;
    }

    public static String getItemUID(URL url) {
        return BluetoothSmartBindingConstants.THING_TYPE_BLE.getAsString().replace(":", "_") + "_" +
                getThingUID(url.getDeviceURL()).replaceAll("-", "_") + "_" + getChannelUID(url).replaceAll("-", "_");
    }

    public static boolean hasNotificationAccess(String[] flags) {
        List<String> flgs = Arrays.asList(flags);
        return flgs.contains(BluetoothSmartBindingConstants.NOTIFY_FLAG) ||
                flgs.contains(BluetoothSmartBindingConstants.INDICATE_FLAG);
    }

    public static boolean hasReadAccess(String[] flags) {
        List<String> flgs = Arrays.asList(flags);
        return flgs.contains(BluetoothSmartBindingConstants.READ_FLAG);
    }

    public static boolean hasWriteAccess(String[] flags) {
        List<String> flgs = Arrays.asList(flags);
        return flgs.contains(BluetoothSmartBindingConstants.WRITE_FLAG);
    }

}
