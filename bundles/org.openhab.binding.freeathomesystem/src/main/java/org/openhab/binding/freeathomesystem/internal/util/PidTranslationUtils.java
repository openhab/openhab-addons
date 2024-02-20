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
package org.openhab.binding.freeathomesystem.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PidTranslationUtils} supporting the translation from pairing IDs into openHAB types
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class PidTranslationUtils {
    private static final Map<String, PIdContainerClass> MAP_TRANSLATOR;

    public static final String PID_VALUETYPE_UNKNOWN = "PID_VALUETYPE_UNKNOWN";
    public static final String PID_VALUETYPE_BOOLEAN = "PID_VALUETYPE_BOOLEAN";
    public static final String PID_VALUETYPE_DECIMAL = "PID_VALUETYPE_DECIMAL";
    public static final String PID_VALUETYPE_INTEGER = "PID_VALUETYPE_INTEGER";
    public static final String PID_VALUETYPE_STRING = "PID_VALUETYPE_STRING";
    public static final String PID_VALUETYPE_SHUTTERMOVEMENT = "PID_VALUETYPE_SHUTTERMOVEMENT";
    public static final String PID_VALUETYPE_ENUM = "PID_VALUETYPE_ENUM";

    public static final String CATEGORY_UNDEFINED = "-";
    public static final String CATEGORY_BATTERY = "Battery";
    public static final String CATEGORY_ALARM = "Alarm";
    public static final String CATEGORY_HUMIDITY = "Humidity";
    public static final String CATEGORY_TEMPERATURE = "Temperature";
    public static final String CATEGORY_MOTION = "Motion";
    public static final String CATEGORY_PRESSURE = "Pressure";
    public static final String CATEGORY_SMOKE = "Smoke";
    public static final String CATEGORY_WATER = "Water";
    public static final String CATEGORY_WIND = "Wind";
    public static final String CATEGORY_RAIN = "Rain";
    public static final String CATEGORY_ENERGY = "Energy";
    public static final String CATEGORY_BLINDS = "Blinds";
    public static final String CATEGORY_CONTACT = "Contact";
    public static final String CATEGORY_SWITCH = "Switch";

    private static PIdContainerClass createFreeAtHomePairingIdTranslation(String p1, String p2, String p3, String p4,
            String p5, String p6) {
        return new PIdContainerClass(p1, p2, p3, p4, p5, p6);
    }

    static {
        Map<String, PIdContainerClass> mapDescObj = new HashMap<String, PIdContainerClass>();

        mapDescObj.put("0x0001", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "0", "1",
                "pid-switch-on-off", "pid-switch-on-off-text"));
        mapDescObj.put("0x0002", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-timed-start-stop", "pid-timed-start-stop-text"));
        mapDescObj.put("0x0003", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-force-position", "pid-force-position-text"));
        mapDescObj.put("0x0004", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-scene-control", "pid-scene-control-text"));
        mapDescObj.put("0x0006",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_MOTION, "", "",
                        "pid-movement-under-consideration-of-brightness",
                        "pid-movement-under-consideration-of-brightness-text"));
        mapDescObj.put("0x0007", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_MOTION, "", "",
                "pid-presence", "pid-presence-text"));
        mapDescObj.put("0x0010", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-relative-set-value", "pid-relative-set-value-text"));
        mapDescObj.put("0x0011", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-absolute-set-value", "pid-absolute-set-value-text"));
        mapDescObj.put("0x0012", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-night", "pid-night-text"));
        mapDescObj.put("0x0013", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-invalid-string-id", "pid-invalid-string-id-text"));
        mapDescObj.put("0x0015", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-rgb-color", "pid-rgb-color-text"));
        mapDescObj.put("0x0016", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-color-temperature", "pid-color-temperature-text"));
        mapDescObj.put("0x0017", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-hsv", "pid-hsv-text"));
        mapDescObj.put("0x0018", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-hue", "pid-hue-text"));
        mapDescObj.put("0x0019", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-saturation", "pid-saturation-text"));
        mapDescObj.put("0x0020", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "pid-move-up-down", "pid-move-up-down-text"));
        mapDescObj.put("0x0021", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "pid-adjust-up-down", "pid-adjust-up-down-text"));
        mapDescObj.put("0x0023", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "pid-set-absolute-position-blinds", "pid-set-absolute-position-blinds-text"));
        mapDescObj.put("0x0024", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "pid-set-absolute-position-slats", "pid-set-absolute-position-slats-text"));
        mapDescObj.put("0x0025", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_WIND, "", "",
                "pid-wind-alarm", "pid-wind-alarm-text"));
        mapDescObj.put("0x0026", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_TEMPERATURE, "",
                "", "pid-frost-alarm", "pid-frost-alarm-text"));
        mapDescObj.put("0x0027", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_RAIN, "", "",
                "pid-rain-alarm", "pid-rain-alarm-text"));
        mapDescObj.put("0x0028", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "pid-force-position-blind", "pid-force-position-blind-text"));
        mapDescObj.put("0x0029", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-window-door-position", "pid-window-door-position-text"));
        mapDescObj.put("0x0030", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-actuating-value-heating", "pid-actuating-value-heating-text"));
        mapDescObj.put("0x0031", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-fan-level-heating", "pid-fan-level-heating-text"));
        mapDescObj.put("0x0032", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-actuating-value-cooling", "pid-actuating-value-cooling-text"));
        mapDescObj.put("0x0033", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "pid-set-value-temperature", "pid-set-value-temperature-text"));
        mapDescObj.put("0x0034", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "pid-relative-set-point-temperature", "pid-relative-set-point-temperature-text"));
        mapDescObj.put("0x0035", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-window-door", "pid-window-door-text"));
        mapDescObj.put("0x0036", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-status-indication", "pid-status-indication-text"));
        mapDescObj.put("0x0037", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-fan-manual-heating-on-off", "pid-fan-manual-heating-on-off-text"));
        mapDescObj.put("0x0038", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-controller-on-off", "pid-controller-on-off-text"));
        mapDescObj.put("0x0039", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "pid-relative-set-point-request", "pid-relative-set-point-request-text"));
        mapDescObj.put("0x003A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-eco-mode-on-off-request", "pid-eco-mode-on-off-request-text"));
        mapDescObj.put("0x003B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "pid-comfort-temperature", "pid-comfort-temperature-text"));
        mapDescObj.put("0x0040", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-fan-level-request", "pid-fan-level-request-text"));
        mapDescObj.put("0x0041", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-fan-manual-on-off-request", "pid-fan-manual-on-off-request-text"));
        mapDescObj.put("0x0042", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-controller-on-off-request", "pid-controller-on-off-request-text"));
        mapDescObj.put("0x0044", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-eco-mode-on-off-request", "pid-eco-mode-on-off-request-text"));
        mapDescObj.put("0x0100", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-info-on-off", "pid-info-on-off-text"));
        mapDescObj.put("0x0101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "pid-force-position-info", "pid-force-position-info-text"));
        mapDescObj.put("0x0105", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infoonoff", "pid-sysap-infoonoff-text"));
        mapDescObj.put("0x0106", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infoforce", "pid-sysap-infoforce-text"));
        mapDescObj.put("0x0110", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-info-actual-dimming-value", "pid-info-actual-dimming-value-text"));
        mapDescObj.put("0x0111", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-info-error", "pid-info-error-text"));
        mapDescObj.put("0x0115", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infocurrentdimmingvalue", "pid-sysap-infocurrentdimmingvalue-text"));
        mapDescObj.put("0x0116", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infoerror", "pid-sysap-infoerror-text"));
        mapDescObj.put("0x0118", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-color-temperature", "pid-info-color-temperature-text"));
        mapDescObj.put("0x011A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-info-color-temperature", "pid-sysap-info-color-temperature-text"));
        mapDescObj.put("0x011B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-hsv", "pid-info-hsv-text"));
        mapDescObj.put("0x011C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-info-hsv", "pid-sysap-info-hsv-text"));
        mapDescObj.put("0x011D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-color-mode", "pid-info-color-mode-text"));
        mapDescObj.put("0x011E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-info-color-mode", "pid-sysap-info-color-mode-text"));
        mapDescObj.put("0x0120", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "pid-info-move-up-down", "pid-info-move-up-down-text"));
        mapDescObj.put("0x0121",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "", "",
                        "pid-current-absolute-position-blinds-percentage",
                        "pid-current-absolute-position-blinds-percentage-text"));
        mapDescObj.put("0x0122",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "pid-current-absolute-position-slats-percentage",
                        "pid-current-absolute-position-slats-percentage-text"));
        mapDescObj.put("0x0125", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infomoveupdown", "pid-sysap-infomoveupdown-text"));
        mapDescObj.put("0x0126", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infocurrentabsoluteblindspercentage", "pid-sysap-infocurrentabsoluteblindspercentage-text"));
        mapDescObj.put("0x0127", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infocurrentabsoluteslatspercentage", "pid-sysap-infocurrentabsoluteslatspercentage-text"));
        mapDescObj.put("0x0130", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "pid-measured-temperature", "pid-measured-temperature-text"));
        mapDescObj.put("0x0131", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-value-heating", "pid-info-value-heating-text"));
        mapDescObj.put("0x0132", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-info-value-cooling", "pid-info-value-cooling-text"));
        mapDescObj.put("0x0135", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-switchover-heating-cooling", "pid-switchover-heating-cooling-text"));
        mapDescObj.put("0x0136", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-actuating-fan-stage-heating", "pid-actuating-fan-stage-heating-text"));
        mapDescObj.put("0x0140", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "pid-absolute-setpoint-temperature", "pid-absolute-setpoint-temperature-text"));
        mapDescObj.put("0x0141", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-additional-heating-value-info", "pid-additional-heating-value-info-text"));
        mapDescObj.put("0x0142", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-additional-cooling-value-info", "pid-additional-cooling-value-info-text"));
        mapDescObj.put("0x0143", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-control-value-additional-heating", "pid-control-value-additional-heating-text"));
        mapDescObj.put("0x0144", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-control-value-additional-cooling", "pid-control-value-additional-cooling-text"));
        mapDescObj.put("0x0145", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-actuating-fan-stage-heating", "pid-info-actuating-fan-stage-heating-text"));
        mapDescObj.put("0x0146", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-actuating-fan-manual-on-off-heating", "pid-info-actuating-fan-manual-on-off-heating-text"));
        mapDescObj.put("0x0147", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-actuating-fan-stage-cooling", "pid-actuating-fan-stage-cooling-text"));
        mapDescObj.put("0x0149", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-info-fan-stage-cooling", "pid-info-fan-stage-cooling-text"));
        mapDescObj.put("0x014A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_TEMPERATURE, "",
                "", "pid-info-fan-manual-on-off-cooling", "pid-info-fan-manual-on-off-cooling-text"));
        mapDescObj.put("0x014B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-heating-active", "pid-heating-active-text"));
        mapDescObj.put("0x014C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-cooling-active", "pid-cooling-active-text"));
        mapDescObj.put("0x014D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-heating-demand", "pid-heating-demand-text"));
        mapDescObj.put("0x014E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-cooling-demand", "pid-cooling-demand-text"));
        mapDescObj.put("0x014F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-heating-demand-feedback-signal", "pid-heating-demand-feedback-signal-text"));
        mapDescObj.put("0x0150", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "pid-cooling-demand-feedback-signal", "pid-cooling-demand-feedback-signal-text"));
        mapDescObj.put("0x0151", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_HUMIDITY, "", "",
                "pid-humidity", "pid-humidity-text"));
        mapDescObj.put("0x0152", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-aux-on-off-request", "pid-aux-on-off-request-text"));
        mapDescObj.put("0x0153", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-aux-on-off-response", "pid-aux-on-off-response-text"));
        mapDescObj.put("0x0154", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-heating-on-off-request", "pid-heating-on-off-request-text"));
        mapDescObj.put("0x0155", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-cooling-on-off-request", "pid-cooling-on-off-request-text"));
        mapDescObj.put("0x0156", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-operation-mode", "pid-operation-mode-text"));
        mapDescObj.put("0x0157", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-swing-h-v", "pid-swing-h-v-text"));
        mapDescObj.put("0x0158", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-supported-features", "pid-supported-features-text"));
        mapDescObj.put("0x0159", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-extended-status-indication", "pid-extended-status-indication-text"));
        mapDescObj.put("0x015A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-extended-status-indication", "pid-extended-status-indication-text"));
        mapDescObj.put("0x015B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-aux-heating-on-off-request", "pid-aux-heating-on-off-request-text"));
        mapDescObj.put("0x015C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-emergency-heating-on-off-request", "pid-emergency-heating-on-off-request-text"));
        mapDescObj.put("0x0160", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-relative-fan-speed-control", "pid-relative-fan-speed-control-text"));
        mapDescObj.put("0x0161", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-absolute-fan-speed-control", "pid-absolute-fan-speed-control-text"));
        mapDescObj.put("0x0162", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-absolute-fan-speed", "pid-info-absolute-fan-speed-text"));
        mapDescObj.put("0x0163", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-sysap-infoactualfanspeed", "pid-sysap-infoactualfanspeed-text"));
        mapDescObj.put("0x01A0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-notification-flags", "pid-notification-flags-text"));
        mapDescObj.put("0x0280", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-power-rc", "pid-power-rc-text"));
        mapDescObj.put("0x0281", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-power-rh", "pid-power-rh-text"));
        mapDescObj.put("0x0282", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-proximity-status", "pid-proximity-status-text"));
        mapDescObj.put("0x0290", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-brightness-sensor", "pid-brightness-sensor-text"));
        mapDescObj.put("0x0291", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-last-touch", "pid-last-touch-text"));
        mapDescObj.put("0x0292", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-led-backlighting-night-mode", "pid-led-backlighting-night-mode-text"));
        mapDescObj.put("0x02C0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-locator-beep", "pid-locator-beep-text"));
        mapDescObj.put("0x02C1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-switch-test-alarm", "pid-switch-test-alarm-text"));
        mapDescObj.put("0x02C3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-fire-alarm-active", "pid-fire-alarm-active-text"));
        mapDescObj.put("0x0400", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "",
                "", "pid-outside-temperature", "pid-outside-temperature-text"));
        mapDescObj.put("0x0401", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_WIND, "", "",
                "pid-wind-force", "pid-wind-force-text"));
        mapDescObj.put("0x0402", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "pid-brightness-alarm", "pid-brightness-alarm-text"));
        mapDescObj.put("0x0403", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_UNDEFINED, "", "",
                "pid-lux-value", "pid-lux-value-text"));
        mapDescObj.put("0x0404", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_WIND, "", "",
                "pid-wind-speed", "pid-wind-speed-text"));
        mapDescObj.put("0x0405", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_RAIN, "", "",
                "pid-rain-detection", "pid-rain-detection-text"));
        mapDescObj.put("0x0406", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_RAIN, "", "",
                "pid-rain-sensor-frequency", "pid-rain-sensor-frequency-text"));
        mapDescObj.put("0x0440", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-play", "pid-play-text"));
        mapDescObj.put("0x0441", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-pause", "pid-pause-text"));
        mapDescObj.put("0x0442", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-next", "pid-next-text"));
        mapDescObj.put("0x0443", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-previous", "pid-previous-text"));
        mapDescObj.put("0x0444", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-play-mode", "pid-play-mode-text"));
        mapDescObj.put("0x0445", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-mute", "pid-mute-text"));
        mapDescObj.put("0x0446", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-relative-volume-control", "pid-relative-volume-control-text"));
        mapDescObj.put("0x0447", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-absolute-volume-control", "pid-absolute-volume-control-text"));
        mapDescObj.put("0x0448", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-group-membership", "pid-group-membership-text"));
        mapDescObj.put("0x0449", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-play-favorite", "pid-play-favorite-text"));
        mapDescObj.put("0x044A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-play-next-favorite", "pid-play-next-favorite-text"));
        mapDescObj.put("0x0460", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-playback-status", "pid-playback-status-text"));
        mapDescObj.put("0x0461", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-current-item-metadata-info", "pid-current-item-metadata-info-text"));
        mapDescObj.put("0x0462", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-mute", "pid-info-mute-text"));
        mapDescObj.put("0x0463", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-actual-volume", "pid-info-actual-volume-text"));
        mapDescObj.put("0x0464", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-allowed-playback-actions", "pid-allowed-playback-actions-text"));
        mapDescObj.put("0x0465", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-group-membership", "pid-info-group-membership-text"));
        mapDescObj.put("0x0466", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-playing-favorite", "pid-info-playing-favorite-text"));
        mapDescObj.put("0x0467", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-absolute-group-volume-control", "pid-absolute-group-volume-control-text"));
        mapDescObj.put("0x0468", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-absolute-group-volume", "pid-info-absolute-group-volume-text"));
        mapDescObj.put("0x0469", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-media-source", "pid-media-source-text"));
        mapDescObj.put("0x04A0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-solar-power-production", "pid-solar-power-production-text"));
        mapDescObj.put("0x04A1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-inverter-output-power", "pid-inverter-output-power-text"));
        mapDescObj.put("0x04A2", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-solar-energy-(today)", "pid-solar-energy-(today)-text"));
        mapDescObj.put("0x04A3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-injected-energy-(today)", "pid-injected-energy-(today)-text"));
        mapDescObj.put("0x04A4", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-purchased-energy-(today)", "pid-purchased-energy-(today)-text"));
        mapDescObj.put("0x04A5", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-inverter-alarm", "pid-inverter-alarm-text"));
        mapDescObj.put("0x04A6", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-self-consumption", "pid-self-consumption-text"));
        mapDescObj.put("0x04A7", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-self-sufficiency", "pid-self-sufficiency-text"));
        mapDescObj.put("0x04A8", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-home-power-consumption", "pid-home-power-consumption-text"));
        mapDescObj.put("0x04A9", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-power-to-grid", "pid-power-to-grid-text"));
        mapDescObj.put("0x04AA", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-consumed-energy-(today)", "pid-consumed-energy-(today)-text"));
        mapDescObj.put("0x04AB", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-meter-alarm", "pid-meter-alarm-text"));
        mapDescObj.put("0x04AC", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-battery-level", "pid-battery-level-text"));
        mapDescObj.put("0x04AD", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-battery-power", "pid-battery-power-text"));
        mapDescObj.put("0x04B0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-boost", "pid-boost-text"));
        mapDescObj.put("0x04B1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-stop-charging-reuqest", "pid-stop-charging-reuqest-text"));
        mapDescObj.put("0x04B2", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-enable-charging-reuqest", "pid-enable-charging-reuqest-text"));
        mapDescObj.put("0x04B3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-boost", "pid-info-boost-text"));
        mapDescObj.put("0x04B4", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-wallbox-status", "pid-info-wallbox-status-text"));
        mapDescObj.put("0x04B5", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-charging", "pid-info-charging-text"));
        mapDescObj.put("0x04B6", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-charging-enabled", "pid-info-charging-enabled-text"));
        mapDescObj.put("0x04B7", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-installed-power", "pid-info-installed-power-text"));
        mapDescObj.put("0x04B8", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-transmitted-energy", "pid-info-transmitted-energy-text"));
        mapDescObj.put("0x04B9", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-car-range", "pid-info-car-range-text"));
        mapDescObj.put("0x04BA", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-charging-duration", "pid-info-charging-duration-text"));
        mapDescObj.put("0x04BB", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-current-limit", "pid-info-current-limit-text"));
        mapDescObj.put("0x04BC", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-current-limit-for-group", "pid-info-current-limit-for-group-text"));
        mapDescObj.put("0x04BD", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-album-cover-url", "pid-album-cover-url-text"));
        mapDescObj.put("0x0501", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-secure@home-central-unit", "pid-secure@home-central-unit-text"));
        mapDescObj.put("0x0502", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-domusdisarmcounter", "pid-domusdisarmcounter-text"));
        mapDescObj.put("0x0504", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-intrusion-alarm", "pid-intrusion-alarm-text"));
        mapDescObj.put("0x0505", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-safety-alarm", "pid-safety-alarm-text"));
        mapDescObj.put("0x0507", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-infoconfigurationstatus", "pid-infoconfigurationstatus-text"));
        mapDescObj.put("0x0508", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-enable-configuration", "pid-enable-configuration-text"));
        mapDescObj.put("0x0509", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-disarming-led", "pid-disarming-led-text"));
        mapDescObj.put("0x050A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-aes-key", "pid-aes-key-text"));
        mapDescObj.put("0x050B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-zone-status", "pid-zone-status-text"));
        mapDescObj.put("0x050E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-time", "pid-time-text"));
        mapDescObj.put("0x0600", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-start-stop", "pid-start-stop-text"));
        mapDescObj.put("0x0601", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-pause-resume", "pid-pause-resume-text"));
        mapDescObj.put("0x0602", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-select-program", "pid-select-program-text"));
        mapDescObj.put("0x0603", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-delayed-start-time", "pid-delayed-start-time-text"));
        mapDescObj.put("0x0604", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-status", "pid-info-status-text"));
        mapDescObj.put("0x0605", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-remote-start-enabled", "pid-info-remote-start-enabled-text"));
        mapDescObj.put("0x0606", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-program", "pid-info-program-text"));
        mapDescObj.put("0x0607", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-finish-time", "pid-info-finish-time-text"));
        mapDescObj.put("0x0608", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-delayed-start", "pid-info-delayed-start-text"));
        mapDescObj.put("0x0609", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-door", "pid-info-door-text"));
        mapDescObj.put("0x060A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-door-alarm", "pid-info-door-alarm-text"));
        mapDescObj.put("0x060B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-switch-supercool", "pid-switch-supercool-text"));
        mapDescObj.put("0x060C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-switch-superfreeze", "pid-switch-superfreeze-text"));
        mapDescObj.put("0x060D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-switch-supercool", "pid-info-switch-supercool-text"));
        mapDescObj.put("0x060E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-switch-superfreeze", "pid-info-switch-superfreeze-text"));
        mapDescObj.put("0x060F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-measured-temperature", "pid-measured-temperature-text"));
        mapDescObj.put("0x0610", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-measured-temperature", "pid-measured-temperature-text"));
        mapDescObj.put("0x0611", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-set-value-temperature", "pid-set-value-temperature-text"));
        mapDescObj.put("0x0612", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-set-value-temperature", "pid-set-value-temperature-text"));
        mapDescObj.put("0x0613", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-change-operation", "pid-change-operation-text"));
        mapDescObj.put("0x0614", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-detailed-status-info", "pid-detailed-status-info-text"));
        mapDescObj.put("0x0615", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-remaining-time", "pid-info-remaining-time-text"));
        mapDescObj.put("0x0616", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-time-of-last-status-change-(star", "pid-time-of-last-status-change-(star-text"));
        mapDescObj.put("0x0618", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-lock-unlock-door-command", "pid-lock-unlock-door-command-text"));
        mapDescObj.put("0x0619", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-locked-unlocked", "pid-info-locked-unlocked-text"));
        mapDescObj.put("0xF001", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-time", "pid-time-text"));
        mapDescObj.put("0xF002", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-date", "pid-date-text"));
        mapDescObj.put("0xF003", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-notification", "pid-notification-text"));
        mapDescObj.put("0xF101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-switch-entity-on-off", "pid-switch-entity-on-off-text"));
        mapDescObj.put("0xF102", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "pid-info-switch-entity-on-off", "pid-info-switch-entity-on-off-text"));
        mapDescObj.put("0xF104", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-consistency-tag", "pid-consistency-tag-text"));
        mapDescObj.put("0xF105", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-battery-status", "pid-battery-status-text"));
        mapDescObj.put("0xF106", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-stay-awake", "pid-stay-awake-text"));
        mapDescObj.put("0xF107", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-proxy-switch", "pid-proxy-switch-text"));
        mapDescObj.put("0xF108", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-proxy1", "pid-proxy1-text"));
        mapDescObj.put("0xF109", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-proxy2", "pid-proxy2-text"));
        mapDescObj.put("0xF10A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-proxy4", "pid-proxy4-text"));
        mapDescObj.put("0xF10B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-cyclic-sleep-time", "pid-cyclic-sleep-time-text"));
        mapDescObj.put("0xF10C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-presence", "pid-presence-text"));
        mapDescObj.put("0xF10D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-measured-temperature-1", "pid-measured-temperature-1-text"));
        mapDescObj.put("0xF10E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-standby-statistics", "pid-standby-statistics-text"));
        mapDescObj.put("0xF10F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-heartbeat-delay", "pid-heartbeat-delay-text"));
        mapDescObj.put("0xF110", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-info-heartbeat-delay", "pid-info-heartbeat-delay-text"));
        mapDescObj.put("0xFF01", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-measured-temperature-1", "pid-measured-temperature-1-text"));
        mapDescObj.put("0xFF02", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-measured-temperature-2", "pid-measured-temperature-2-text"));
        mapDescObj.put("0xFF03", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-measured-temperature-3", "pid-measured-temperature-3-text"));
        mapDescObj.put("0xFF04", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "pid-measured-temperature-4", "pid-measured-temperature-4-text"));
        mapDescObj.put("0x061A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_PRESSURE, "", "",
                "pid-air-quality-pressure-value", "pid-air-quality-pressure-value-text"));
        mapDescObj.put("0x061B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-air-quality-co2-value", "pid-air-quality-co2-value-text"));
        mapDescObj.put("0x061C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-air-quality-co-value", "pid-air-quality-co-value-text"));
        mapDescObj.put("0x061D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-air-quality-no2-value", "pid-air-quality-no2-value-text"));
        mapDescObj.put("0x061E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-air-quality-o3-value", "pid-air-quality-o3-value-text"));
        mapDescObj.put("0x061F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-air-quality-pm10-value", "pid-air-quality-pm10-value-text"));
        mapDescObj.put("0x0620", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-air-quality-pm25-value", "pid-air-quality-pm25-value-text"));
        mapDescObj.put("0x0621", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "pid-air-quality-voc-value", "pid-air-quality-voc-value-text"));

        MAP_TRANSLATOR = Collections.unmodifiableMap(mapDescObj);
    }

    public static String getShortTextForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.label : "Unknown label";
    }

    public static String getDescriptionTextForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.description : "Unknown description";
    }

    public static String getValueTypeForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.valueType : PID_VALUETYPE_DECIMAL;
    }

    public static String getItemTypeForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.category : CATEGORY_UNDEFINED;
    }

    public static String getCategoryForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.category : CATEGORY_UNDEFINED;
    }

    public static String getPatternForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.category : CATEGORY_UNDEFINED;
    }

    public static int getMax(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.max : 1;
    }

    public static int getMin(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return (desc != null) ? desc.min : 0;
    }
}
