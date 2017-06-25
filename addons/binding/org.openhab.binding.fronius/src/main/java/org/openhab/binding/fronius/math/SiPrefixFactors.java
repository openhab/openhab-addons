package org.openhab.binding.fronius.math;

/**
 * Helper class for unit conversions
 *
 * @author Thomas Rokohl
 *
 */
public class SiPrefixFactors {

    /**
     * return the relative factor to the base unit
     * k == 1000, M = 1000000 ...
     * Not completely!!! Rang from n to T
     *
     * @param prefix
     * @return
     */

    public static double getFactorToBaseUnit(String prefix) {
        if (prefix.isEmpty()) {
            return 1;
        }
        switch (prefix) {
            case "T":
                return 1000000000000d;
            case "G":
                return 1000000000;
            case "M":
                return 1000000;
            case "k":
                return 1000;
            case "h":
                return 100;
            case "da":
                return 10;
            case "d":
                return 0.1;
            case "c":
                return 0.01;
            case "m":
                return 0.001;
            case "µ":
                return 0.000001;
            case "n":
                return 0.000000001;
        }
        return 1;

    }
}
