/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pentair.internal.handler.helpers;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.parser.PentairBasePacket;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliChem } class contains key values from the Pentair IntelliChem.
 *
 * @author Jeff James - initial contribution.
 *
 */
@NonNullByDefault
public class PentairIntelliChem {
    private final Logger logger = LoggerFactory.getLogger(PentairIntelliChem.class);

    private static final int PHREADINGHI = 0 + PentairStandardPacket.STARTOFDATA;
    private static final int PHREADINGLO = 1 + PentairStandardPacket.STARTOFDATA;
    private static final int ORPREADINGHI = 2 + PentairStandardPacket.STARTOFDATA;
    private static final int ORPREADINGLO = 3 + PentairStandardPacket.STARTOFDATA;
    private static final int PHSETPOINTHI = 4 + PentairStandardPacket.STARTOFDATA;
    private static final int PHSETPOINTLO = 5 + PentairStandardPacket.STARTOFDATA;
    private static final int ORPSETPOINTHI = 6 + PentairStandardPacket.STARTOFDATA;
    private static final int ORPSETPOINTLO = 7 + PentairStandardPacket.STARTOFDATA;
    private static final int PHDOSETIMEHI = 10 + PentairStandardPacket.STARTOFDATA;
    private static final int PHDOSETIMELO = 11 + PentairStandardPacket.STARTOFDATA;
    private static final int ORPDOSETIMEHI = 14 + PentairStandardPacket.STARTOFDATA;
    private static final int ORPDOSETIMELO = 15 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int PHVOLUMEDOSEDHI = 16 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int PHVOLUMEDOSEDLO = 17 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int ORPVOLUMEDOSEDHI = 18 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int ORPVOLUMEDOSEDLO = 19 + PentairStandardPacket.STARTOFDATA;
    private static final int TANK1LEVEL = 20 + PentairStandardPacket.STARTOFDATA;
    private static final int TANK2LEVEL = 21 + PentairStandardPacket.STARTOFDATA;
    private static final int LSI = 22 + PentairStandardPacket.STARTOFDATA;
    private static final int CALCIUMHARDNESSHI = 23 + PentairStandardPacket.STARTOFDATA;
    private static final int CALCIUMHARDNESSLO = 24 + PentairStandardPacket.STARTOFDATA;
    private static final int CYAREADING = 26 + PentairStandardPacket.STARTOFDATA;
    private static final int ALKALINITYHI = 27 + PentairStandardPacket.STARTOFDATA;
    private static final int ALKALINITYLO = 28 + PentairStandardPacket.STARTOFDATA;
    private static final int SALTLEVEL = 29 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int TEMPERATURE = 31 + PentairStandardPacket.STARTOFDATA;
    private static final int ALARMS = 32 + PentairStandardPacket.STARTOFDATA;
    private static final int WARNINGS = 33 + PentairStandardPacket.STARTOFDATA;
    private static final int DOSER_TYPE_STATUS = 34 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int DELAYS = 35 + PentairStandardPacket.STARTOFDATA;
    private static final int FIRMWAREMINOR = 36 + PentairStandardPacket.STARTOFDATA;
    private static final int FIRMWAREMAJOR = 37 + PentairStandardPacket.STARTOFDATA;

    public enum PhDoserType {
        NONE,
        CO2,
        ACID;

        private static PhDoserType getType(int num) {
            switch (num) {
                case 0:
                    return NONE;
                case 1:
                    return ACID;
                case 2:
                    return CO2;
                case 3:
                    return ACID;

            }
            return NONE;
        }
    }

    public enum OrpDoserType {
        NONE,
        ORP;

        private static OrpDoserType getType(int num) {
            if (num == 0) {
                return NONE;
            }

            return ORP;
        }
    }

    public enum DosingStatus {
        NONE,
        DOSING,
        MIXING,
        MONITORING;

        private static DosingStatus getType(int num, boolean enabled) {
            if (!enabled) {
                return NONE;
            }

            switch (num) {
                case 0:
                    return DOSING;
                case 1:
                    return MIXING;
                case 2:
                    return MONITORING;
            }

            return NONE;
        }
    }

    public double phReading;
    public int orpReading;
    public double phSetPoint;
    public int orpSetPoint; // Oxidation Reduction Potential
    public int tank1Level;
    public int tank2Level;
    public int calciumHardness;
    public int cyaReading; // Cyanuric Acid
    public int alkalinity;

    public boolean alarmWaterFlow;
    public boolean alarmPh;
    public boolean alarmOrp;
    public boolean alarmPhTank;
    public boolean alarmOrpTank;
    public boolean alarmProbeFault;

    public boolean warningPhLockout;
    public boolean warningPhDailyLimitReached;
    public boolean warningOrpDailyLimitReached;
    public boolean warningInvalidSetup;
    public boolean warningChlorinatorCommError;

    public double lsi;
    public PhDoserType phDoserType = PhDoserType.NONE;
    public OrpDoserType orpDoserType = OrpDoserType.NONE;
    public DosingStatus phDoserStatus = DosingStatus.NONE;
    public DosingStatus orpDoserStatus = DosingStatus.NONE;
    public int phDoseTime;
    public int orpDoseTime;
    public int saltLevel;

    public String firmwareVersion = "";

    public double calcCalciumHardnessFactor() {
        double calciumHardnessFactor = 0;

        if (calciumHardness <= 25) {
            calciumHardnessFactor = 1.0;
        } else if (calciumHardness <= 50) {
            calciumHardnessFactor = 1.3;
        } else if (calciumHardness <= 75) {
            calciumHardnessFactor = 1.5;
        } else if (calciumHardness <= 100) {
            calciumHardnessFactor = 1.6;
        } else if (calciumHardness <= 125) {
            calciumHardnessFactor = 1.7;
        } else if (calciumHardness <= 150) {
            calciumHardnessFactor = 1.8;
        } else if (calciumHardness <= 200) {
            calciumHardnessFactor = 1.9;
        } else if (calciumHardness <= 250) {
            calciumHardnessFactor = 2.0;
        } else if (calciumHardness <= 300) {
            calciumHardnessFactor = 2.1;
        } else if (calciumHardness <= 400) {
            calciumHardnessFactor = 2.2;
        } else if (calciumHardness <= 800) {
            calciumHardnessFactor = 2.5;
        }

        return calciumHardnessFactor;
    }

    public double calcTemperatureFactor(QuantityType<Temperature> t) {
        double temperatureFactor = 0;
        int temperature = t.intValue();

        if (t.getUnit().equals(SIUnits.CELSIUS)) {
            if (temperature <= 0) {
                temperatureFactor = 0.0;
            } else if (temperature <= 2.8) {
                temperatureFactor = 0.1;
            } else if (temperature <= 7.8) {
                temperatureFactor = 0.2;
            } else if (temperature <= 11.7) {
                temperatureFactor = 0.3;
            } else if (temperature <= 15.6) {
                temperatureFactor = 0.4;
            } else if (temperature <= 18.9) {
                temperatureFactor = 0.5;
            } else if (temperature <= 24.4) {
                temperatureFactor = 0.6;
            } else if (temperature <= 28.9) {
                temperatureFactor = 0.7;
            } else if (temperature <= 34.4) {
                temperatureFactor = 0.8;
            } else if (temperature <= 40.6) {
                temperatureFactor = 0.9;
            }
        } else { // Fahrenheit
            if (temperature <= 32) {
                temperatureFactor = 0.0;
            } else if (temperature <= 37) {
                temperatureFactor = 0.1;
            } else if (temperature <= 46) {
                temperatureFactor = 0.2;
            } else if (temperature <= 53) {
                temperatureFactor = 0.3;
            } else if (temperature <= 60) {
                temperatureFactor = 0.4;
            } else if (temperature <= 66) {
                temperatureFactor = 0.5;
            } else if (temperature <= 76) {
                temperatureFactor = 0.6;
            } else if (temperature <= 84) {
                temperatureFactor = 0.7;
            } else if (temperature <= 94) {
                temperatureFactor = 0.8;
            } else if (temperature <= 105) {
                temperatureFactor = 0.9;
            }
        }

        return temperatureFactor;
    }

    public double calcCorrectedAlkalinity() {
        return alkalinity - cyaReading / 3;
    }

    public double calcAlkalinityFactor() {
        double ppm = calcCorrectedAlkalinity();
        double alkalinityFactor = 0;

        if (ppm <= 25) {
            alkalinityFactor = 1.4;
        } else if (ppm <= 50) {
            alkalinityFactor = 1.7;
        } else if (ppm <= 75) {
            alkalinityFactor = 1.9;
        } else if (ppm <= 100) {
            alkalinityFactor = 2.0;
        } else if (ppm <= 125) {
            alkalinityFactor = 2.1;
        } else if (ppm <= 150) {
            alkalinityFactor = 2.2;
        } else if (ppm <= 200) {
            alkalinityFactor = 2.3;
        } else if (ppm <= 250) {
            alkalinityFactor = 2.4;
        } else if (ppm <= 300) {
            alkalinityFactor = 2.5;
        } else if (ppm <= 400) {
            alkalinityFactor = 2.6;
        } else if (ppm <= 800) {
            alkalinityFactor = 2.9;
        }

        return alkalinityFactor;
    }

    public double calcTotalDisovledSolidsFactor(boolean saltPool) {
        // 12.1 for non-salt; 12.2 for salt

        if (saltPool) {
            return 12.2;
        }

        return 12.1;
    }

    public double calcSaturationIndex(@Nullable QuantityType<Temperature> waterTemp, boolean saltPool) {
        double alkalinityFactor;
        double temperatureFactor = .4; // if no temperature is available, use default value of .4
        double saturationIndex;

        if (waterTemp != null) {
            temperatureFactor = calcTemperatureFactor(waterTemp);
        }

        alkalinityFactor = calcAlkalinityFactor();

        saturationIndex = this.phReading + calcCalciumHardnessFactor() + alkalinityFactor + temperatureFactor
                - calcTotalDisovledSolidsFactor(saltPool);

        return saturationIndex;
    }

    /**
     * parsePacket - This function will parse a IntelliChem status packet. Note, this is based on the efforts of the
     * nodejs-poolController utility since this is not equipment that I have and only minimally tested by the community.
     *
     * @param p - PentairPacket to parse
     */
    public void parsePacket(PentairBasePacket packet) {
        PentairStandardPacket p = (PentairStandardPacket) packet;

        if (p.getPacketLengthHeader() != 41) {
            logger.debug("Intellichem packet not 41 bytes long");
            return;
        }

        phReading = ((p.getByte(PHREADINGHI) << 8) + p.getByte(PHREADINGLO)) / 100.0;
        orpReading = (p.getByte(ORPREADINGHI) << 8) + p.getByte(ORPREADINGLO);
        phSetPoint = ((p.getByte(PHSETPOINTHI) << 8) + p.getByte(PHSETPOINTLO)) / 100.0;
        orpSetPoint = (p.getByte(ORPSETPOINTHI) << 8) + p.getByte(ORPSETPOINTLO);
        tank1Level = p.getByte(TANK1LEVEL); // should be value between 1-7
        tank2Level = p.getByte(TANK2LEVEL);
        calciumHardness = (p.getByte(CALCIUMHARDNESSHI) << 8) + p.getByte(CALCIUMHARDNESSLO);
        cyaReading = p.getByte(CYAREADING);
        alkalinity = (p.getByte(ALKALINITYHI) << 8) + p.getByte(ALKALINITYLO);
        phDoserType = PhDoserType.getType(p.getByte(DOSER_TYPE_STATUS) & 0x03);
        orpDoserType = OrpDoserType.getType((p.getByte(DOSER_TYPE_STATUS) & 0x0C) >> 2);
        phDoserStatus = DosingStatus.getType((p.getByte(DOSER_TYPE_STATUS) & 0x30) >> 4,
                phDoserType != PhDoserType.NONE);
        orpDoserStatus = DosingStatus.getType((p.getByte(DOSER_TYPE_STATUS) & 0xC0) >> 6,
                orpDoserType != OrpDoserType.NONE);
        lsi = ((p.getByte(LSI) & 0x80) != 0) ? (256 - p.getByte(LSI)) / -100.0 : p.getByte(LSI) / 100.0;
        phDoseTime = (p.getByte(PHDOSETIMEHI) << 8) + p.getByte(PHDOSETIMELO);
        orpDoseTime = (p.getByte(ORPDOSETIMEHI) << 8) + p.getByte(ORPDOSETIMELO);
        saltLevel = p.getByte(SALTLEVEL) * 50;

        alarmWaterFlow = (p.getByte(ALARMS) & 0x01) != 0;
        alarmPh = (p.getByte(ALARMS) & 0x06) != 0;
        alarmOrp = (p.getByte(ALARMS) & 0x08) != 0;
        alarmPhTank = (p.getByte(ALARMS) & 0x20) != 0;
        alarmOrpTank = (p.getByte(ALARMS) & 0x40) != 0;
        alarmProbeFault = (p.getByte(ALARMS) & 0x80) != 0;

        warningPhLockout = (p.getByte(WARNINGS) & 0x01) != 0;
        warningPhDailyLimitReached = (p.getByte(WARNINGS) & 0x02) != 0;
        warningOrpDailyLimitReached = (p.getByte(WARNINGS) & 0x04) != 0;
        warningInvalidSetup = (p.getByte(WARNINGS) & 0x08) != 0;
        warningChlorinatorCommError = (p.getByte(WARNINGS) & 0x10) != 0;

        firmwareVersion = String.format("%d.%03d", p.getByte(FIRMWAREMAJOR), p.getByte(FIRMWAREMINOR));
    }

    @Override
    public String toString() {
        String str = String.format(
                "PH: %.2f, OPR: %d, PH set point: %.2f, ORP set point: %d, tank1: %d, tank2: %d, calcium hardness: %d, cyareading: %d, alkalinity: %d, phDoserType: %s, orpDoserType: %s, phDoserStatus: %b, orpDoserStatus: %b, phDoseTime: %d, orpDoseTime: %d, saturationindex: %f.1",
                phReading, orpReading, phSetPoint, orpSetPoint, tank1Level, tank2Level, calciumHardness, cyaReading,
                alkalinity, phDoserType.toString(), orpDoserType.toString(), phDoserStatus, orpDoserStatus, phDoseTime,
                orpDoseTime, lsi);

        return str;
    }
}
