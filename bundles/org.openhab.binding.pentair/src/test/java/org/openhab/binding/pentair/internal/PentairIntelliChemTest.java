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
package org.openhab.binding.pentair.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openhab.binding.pentair.internal.TestUtilities.parsehex;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.pentair.internal.handler.helpers.PentairIntelliChem;
import org.openhab.binding.pentair.internal.handler.helpers.PentairIntelliChem.DosingStatus;
import org.openhab.binding.pentair.internal.handler.helpers.PentairIntelliChem.OrpDoserType;
import org.openhab.binding.pentair.internal.handler.helpers.PentairIntelliChem.PhDoserType;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;

/**
 * PentairIntelliChemTest
 *
 * @author Jeff James - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class PentairIntelliChemTest {
    //@formatter:off
    public static byte[][] packets = {
            parsehex("A50010901229030202A302D002C60000000000000000000000000006070000C8003F005A3C00580006A5201E010000"),
            parsehex("A5100F10122902E302AF02EE02BC000000020000002A0004005C060518019000000096140051000065203C01000000"),
            parsehex("A5001090122902E4030202E402BC00000010000000000023000006060300FA002C00A0140051080095005001000000"),
            parsehex("A5001090122902F3030502F802EE0000007900000000000000000000FD01C2005000465200550800A2205001000000"),
            parsehex("A5001090122902EA02DC02F8028A0000000800000000000000000000F4015E0000004652004D0000A2205001000000")
    };
    //@formatter:on

    @Test
    public void test() {
        PentairIntelliChem pic = new PentairIntelliChem();
        PentairStandardPacket p = new PentairStandardPacket(packets[0], packets[0].length);

        pic.parsePacket(p);

        assertThat(pic.phReading, equalTo(7.70));
        assertThat(pic.orpReading, equalTo(675));
        assertThat(pic.phSetPoint, equalTo(7.20));
        assertThat(pic.orpSetPoint, equalTo(710));
        assertThat(pic.tank1Level, equalTo(0));
        assertThat(pic.tank2Level, equalTo(6));
        assertThat(pic.calciumHardness, equalTo(0));
        assertThat(pic.cyaReading, equalTo(0));
        assertThat(pic.alkalinity, equalTo(16128));
        // assertThat(pic.alarmWaterFlow, equalTo(fal));
        assertThat(pic.lsi, equalTo(0.07));
        assertThat(pic.phDoserType, equalTo(PhDoserType.CO2));
        assertThat(pic.orpDoserType, equalTo(OrpDoserType.ORP));
        assertThat(pic.phDoserStatus, equalTo(DosingStatus.DOSING));
        assertThat(pic.orpDoserStatus, equalTo(DosingStatus.DOSING));
        assertThat(pic.phDoseTime, equalTo(0));
        assertThat(pic.orpDoseTime, equalTo(0));
        assertThat(pic.saltLevel, equalTo(4500));
        assertThat(pic.calcCalciumHardnessFactor(), equalTo(1.0));

        assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.alarmPh, equalTo(false));
        assertThat(pic.alarmOrp, equalTo(true));
        assertThat(pic.alarmPhTank, equalTo(false));
        assertThat(pic.alarmOrpTank, equalTo(true));
        assertThat(pic.alarmProbeFault, equalTo(false));

        assertThat(pic.warningPhLockout, equalTo(false));
        assertThat(pic.warningPhDailyLimitReached, equalTo(false));
        assertThat(pic.warningOrpDailyLimitReached, equalTo(false));
        assertThat(pic.warningInvalidSetup, equalTo(false));
        assertThat(pic.warningChlorinatorCommError, equalTo(false));
        assertThat(pic.firmwareVersion, equalTo("30.032"));

        p = new PentairStandardPacket(packets[1], packets[1].length);
        pic.parsePacket(p);

        assertThat(pic.phReading, equalTo(7.39));
        assertThat(pic.orpReading, equalTo(687));
        assertThat(pic.phSetPoint, equalTo(7.50));
        assertThat(pic.orpSetPoint, equalTo(700));
        assertThat(pic.tank1Level, equalTo(6));
        assertThat(pic.tank2Level, equalTo(5));
        assertThat(pic.calciumHardness, equalTo(400));
        assertThat(pic.cyaReading, equalTo(0));
        assertThat(pic.alkalinity, equalTo(150));
        // assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.lsi, equalTo(0.24));
        assertThat(pic.phDoserType, equalTo(PhDoserType.ACID));
        assertThat(pic.orpDoserType, equalTo(OrpDoserType.ORP));
        assertThat(pic.phDoserStatus, equalTo(DosingStatus.MONITORING));
        assertThat(pic.orpDoserStatus, equalTo(DosingStatus.MIXING));
        assertThat(pic.phDoseTime, equalTo(2));
        assertThat(pic.orpDoseTime, equalTo(42));
        assertThat(pic.saltLevel, equalTo(1000));
        assertThat(pic.calcCalciumHardnessFactor(), equalTo(2.2));

        assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.alarmPh, equalTo(false));
        assertThat(pic.alarmOrp, equalTo(false));
        assertThat(pic.alarmPhTank, equalTo(false));
        assertThat(pic.alarmOrpTank, equalTo(false));
        assertThat(pic.alarmProbeFault, equalTo(false));

        assertThat(pic.warningPhLockout, equalTo(false));
        assertThat(pic.warningPhDailyLimitReached, equalTo(false));
        assertThat(pic.warningOrpDailyLimitReached, equalTo(false));
        assertThat(pic.warningInvalidSetup, equalTo(false));
        assertThat(pic.warningChlorinatorCommError, equalTo(false));
        assertThat(pic.firmwareVersion, equalTo("1.060"));

        p = new PentairStandardPacket(packets[2], packets[2].length);
        pic.parsePacket(p);

        assertThat(pic.phReading, equalTo(7.4));
        assertThat(pic.orpReading, equalTo(770));
        assertThat(pic.phSetPoint, equalTo(7.4));
        assertThat(pic.orpSetPoint, equalTo(700));
        assertThat(pic.tank1Level, equalTo(6));
        assertThat(pic.tank2Level, equalTo(6));
        assertThat(pic.calciumHardness, equalTo(250));
        assertThat(pic.cyaReading, equalTo(44));
        assertThat(pic.alkalinity, equalTo(160));
        assertThat(pic.lsi, equalTo(0.03));
        // assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.phDoserType, equalTo(PhDoserType.ACID));
        assertThat(pic.orpDoserType, equalTo(OrpDoserType.ORP));
        assertThat(pic.phDoserStatus, equalTo(DosingStatus.MIXING));
        assertThat(pic.orpDoserStatus, equalTo(DosingStatus.MONITORING));
        assertThat(pic.phDoseTime, equalTo(16));
        assertThat(pic.orpDoseTime, equalTo(0));
        assertThat(pic.saltLevel, equalTo(1000));
        assertThat(pic.calcCalciumHardnessFactor(), equalTo(2.0));

        assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.alarmPh, equalTo(false));
        assertThat(pic.alarmOrp, equalTo(true));
        assertThat(pic.alarmPhTank, equalTo(false));
        assertThat(pic.alarmOrpTank, equalTo(false));
        assertThat(pic.alarmProbeFault, equalTo(false));

        assertThat(pic.warningPhLockout, equalTo(false));
        assertThat(pic.warningPhDailyLimitReached, equalTo(false));
        assertThat(pic.warningOrpDailyLimitReached, equalTo(false));
        assertThat(pic.warningInvalidSetup, equalTo(false));
        assertThat(pic.warningChlorinatorCommError, equalTo(false));
        assertThat(pic.firmwareVersion, equalTo("1.080"));

        p = new PentairStandardPacket(packets[3], packets[3].length);
        pic.parsePacket(p);

        assertThat(pic.phReading, equalTo(7.55));
        assertThat(pic.orpReading, equalTo(773));
        assertThat(pic.phSetPoint, equalTo(7.6));
        assertThat(pic.orpSetPoint, equalTo(750));
        assertThat(pic.tank1Level, equalTo(0));
        assertThat(pic.tank2Level, equalTo(0));
        assertThat(pic.calciumHardness, equalTo(450));
        assertThat(pic.cyaReading, equalTo(80));
        assertThat(pic.alkalinity, equalTo(70));
        assertThat(pic.lsi, equalTo(-0.03));
        // assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.phDoserType, equalTo(PhDoserType.CO2));
        assertThat(pic.orpDoserType, equalTo(OrpDoserType.NONE));
        assertThat(pic.phDoserStatus, equalTo(DosingStatus.MONITORING));
        assertThat(pic.orpDoserStatus, equalTo(DosingStatus.NONE));
        assertThat(pic.phDoseTime, equalTo(121));
        assertThat(pic.orpDoseTime, equalTo(0));
        assertThat(pic.saltLevel, equalTo(4100));
        assertThat(pic.calcCalciumHardnessFactor(), equalTo(2.5));

        assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.alarmPh, equalTo(false));
        assertThat(pic.alarmOrp, equalTo(true));
        assertThat(pic.alarmPhTank, equalTo(false));
        assertThat(pic.alarmOrpTank, equalTo(false));
        assertThat(pic.alarmProbeFault, equalTo(false));

        assertThat(pic.warningPhLockout, equalTo(false));
        assertThat(pic.warningPhDailyLimitReached, equalTo(false));
        assertThat(pic.warningOrpDailyLimitReached, equalTo(false));
        assertThat(pic.warningInvalidSetup, equalTo(false));
        assertThat(pic.warningChlorinatorCommError, equalTo(false));
        assertThat(pic.firmwareVersion, equalTo("1.080"));

        p = new PentairStandardPacket(packets[4], packets[4].length);
        pic.parsePacket(p);

        assertThat(pic.phReading, equalTo(7.46));
        assertThat(pic.orpReading, equalTo(732));
        assertThat(pic.phSetPoint, equalTo(7.6));
        assertThat(pic.orpSetPoint, equalTo(650));
        assertThat(pic.tank1Level, equalTo(0));
        assertThat(pic.tank2Level, equalTo(0));
        assertThat(pic.calciumHardness, equalTo(350));
        assertThat(pic.cyaReading, equalTo(0));
        assertThat(pic.alkalinity, equalTo(70));
        assertThat(pic.lsi, equalTo(-0.12));
        // assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.phDoserType, equalTo(PhDoserType.CO2));
        assertThat(pic.orpDoserType, equalTo(OrpDoserType.NONE));
        assertThat(pic.phDoserStatus, equalTo(DosingStatus.MONITORING));
        assertThat(pic.orpDoserStatus, equalTo(DosingStatus.NONE));
        assertThat(pic.phDoseTime, equalTo(8));
        assertThat(pic.orpDoseTime, equalTo(0));
        assertThat(pic.saltLevel, equalTo(4100));
        assertThat(pic.calcCalciumHardnessFactor(), equalTo(2.2));

        assertThat(pic.alarmWaterFlow, equalTo(false));
        assertThat(pic.alarmPh, equalTo(false));
        assertThat(pic.alarmOrp, equalTo(false));
        assertThat(pic.alarmPhTank, equalTo(false));
        assertThat(pic.alarmOrpTank, equalTo(false));
        assertThat(pic.alarmProbeFault, equalTo(false));

        assertThat(pic.warningPhLockout, equalTo(false));
        assertThat(pic.warningPhDailyLimitReached, equalTo(false));
        assertThat(pic.warningOrpDailyLimitReached, equalTo(false));
        assertThat(pic.warningInvalidSetup, equalTo(false));
        assertThat(pic.warningChlorinatorCommError, equalTo(false));
        assertThat(pic.firmwareVersion, equalTo("1.080"));
    }
}
