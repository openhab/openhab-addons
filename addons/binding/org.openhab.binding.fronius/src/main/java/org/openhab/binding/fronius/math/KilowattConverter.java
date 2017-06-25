package org.openhab.binding.fronius.math;

public class KilowattConverter {

    public static double getConvertFactor(String fromUnit, String toUnit) {
        String adjustedFromUnit = fromUnit.replace("Wh", "");
        String adjustedtoUnit = toUnit.replace("Wh", "");
        return SiPrefixFactors.getFactorToBaseUnit(adjustedFromUnit) * 1 / SiPrefixFactors.getFactorToBaseUnit(adjustedtoUnit);

    }

    public static double convertTo(double value, String fromUnit, String toUnit) {
        return value * getConvertFactor(fromUnit, toUnit);
    }
}
