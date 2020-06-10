package org.openhab.binding.luftdateninfo.internal.utils;

public class NumberUtils {
    public static double round(Object o, int places) {
        // LOGGER.info("Round "+o);
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        Double value = null;
        if (o instanceof Integer) {
            value = (double) ((Integer) o).intValue();
        } else {
            value = (Double) o;
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
