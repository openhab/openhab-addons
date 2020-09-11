/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.senechome.internal;

/**
 * The {@link SenecBatteryStatus} class defines available Senec specific
 * battery states.
 *
 * @author Steven.Schwarznau - Initial contribution
 *
 */
public enum SenecBatteryStatus {

    INITIALSTATE(0),
    ERROR_INVERTER_COMMUNICATION(1),
    ERROR_ELECTRICY_METER(2),
    RIPPLE_CONTROL_RECEIVER(3),
    INITIAL_CHARGE(4),
    MAINTENANCE_CHARGE(5),
    MAINTENANCE_READY(6),
    MAINTENANCE_REQUIRED(7),
    MAN_SAFETY_CHARGE(8),
    SAFETY_CHARGE_READY(9),
    FULL_CHARGE(10),
    EQUALIZATION_CHARGE(11),
    DESULFATATION_CHARGE(12),
    BATTERY_FULL(13),
    CHARGE(14),
    BATTERY_EMPTY(15),
    DISCHARGE(16),
    PV_AND_DISCHARGE(17),
    GRID_AND_DISCHARGE(18),
    PASSIVE(19),
    OFF(20),
    OWN_CONSUMPTION(21),
    RESTART(22),
    MAN_EQUALIZATION_CHARGE(23),
    MAN_DESULFATATION_CHARGE(24),
    SAFETY_CHARGE(25),
    BATTERY_PROTECTION_MODE(26),
    EG_ERROR(27),
    EG_CHARGE(28),
    EG_DISCHARGE(29),
    EG_PASSIVE(30),
    EG_PROHIBIT_CHARGE(31),
    EG_PROHIBIT_DISCHARGE(32),
    EMERGANCY_CHARGE(33),
    SOFTWARE_UPDATE(34),
    NSP_ERROR(35),
    NSP_ERROR_GRID(36),
    NSP_ERROR_HARDWRE(37),
    NO_SERVER_CONNECTION(38),
    BMS_ERROR(39),
    MAINTENANCE_FILTER(40),
    SLEEPING_MODE(41),
    WAITING_EXCESS(42),
    CAPACITY_TEST_CHARGE(43),
    CAPACITY_TEST_DISCHARGE(44),
    MAN_DESULFATATION_WAIT(45),
    MAN_DESULFATATION_READY(46),
    MAN_DESULFATATION_ERROR(47),
    EQUALIZATION_WAIT(48),
    EMERGANCY_CHARGE_ERROR(49),
    MAN_EQUALIZATION_WAIT(50),
    MAN_EQUALIZATION_ERROR(51),
    MAN_EQUALIZATION_READY(52),
    AUTO_DESULFATATION_WAIT(53),
    ABSORPTION_PHASE(54),
    DCSWITCH_OFF(55),
    PEAKSHAVING_WAIT(56),
    ERROR_BATTERY_INVERTER(57),
    NPUERROR(58),
    BMS_OFFLINE(59),
    MAINTENANCE_CHARGE_ERROR(60),
    MAN_SAFETY_CHARGE_ERROR(61),
    SAFETY_CHARGE_ERROR(62),
    NO_CONNECTION_TO_MASTER(63),
    LITHIUM_SAFE_MODE_ACTIVE(64),
    LITHIUM_SAFE_MODE_DONE(65),
    BATTERY_VOLTAGE_ERROR(66),
    BMS_DC_SWITCHED_OFF(67),
    GRID_INITIALIZATION(68),
    GRID_STABILIZATION(69),
    REMOTE_SHUTDOWN(70),
    OFFPEAKCHARGE(71),
    ERROR_HALFBRIDGE(72),
    BMS_ERROR_OPERATING_TEMPERATURE(73),
    FACOTRY_SETTINGS_NOT_FOUND(74),
    BACKUP_POWER_MODE_ACTIVE(75),
    BACKUP_POWER_MODE_BATTERY_EMPTY(76),
    BACKUP_POWER_MODE_ERROR(77),
    INITIALISING(78),
    INSTALLATION_MODE(79),
    GRID_OFFLINE(80),
    BMS_UPDATE_NEEDED(81),
    BMS_CONFIGURATION_NEEDED(82),
    INSULATION_TEST(83),
    SELFTEST(84),
    EXTERNAL_CONTROL(85),
    ERROR_TEMPERATURESENSOR(86),
    GRID_OPERATOR_CHARGE_PROHIBITED(87),
    GRID_OPERATOR_DISCHARGE_PROHIBITED(88),
    SPARE_CAPACITY(89),
    SELFTEST_ERROR(90),
    EARTH_FAULT(91),
    UNKNOWN(-1);

    private int code;

    SenecBatteryStatus(int index) {
        this.code = index;
    }

    public int getCode() {
        return code;
    }

    public static SenecBatteryStatus fromCode(int code) {
        for (SenecBatteryStatus state : SenecBatteryStatus.values()) {
            if (state.code == code) {
                return state;
            }
        }
        return SenecBatteryStatus.UNKNOWN;
    }
}
