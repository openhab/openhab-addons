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
    public static final String PID_VALUETYPE_INTEGER = "PID_VALUETYPE_INTIGER";
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
                "@text/fid-switch-on-off", "@text/fid-switch-on-off-text"));
        mapDescObj.put("0x0002", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-timed-start-stop", "@text/fid-timed-start-stop-text"));
        mapDescObj.put("0x0003", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-force-position", "@text/fid-force-position-text"));
        mapDescObj.put("0x0004", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-scene-control", "@text/fid-scene-control-text"));
        mapDescObj.put("0x0006",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_MOTION, "", "",
                        "@text/fid-movement-under-consideration-of-brightness",
                        "@text/fid-movement-under-consideration-of-brightness-text"));
        mapDescObj.put("0x0007", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_MOTION, "", "",
                "@text/fid-presence", "@text/fid-presence-text"));
        mapDescObj.put("0x0010", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-relative-set-value", "@text/fid-relative-set-value-text"));
        mapDescObj.put("0x0011", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-absolute-set-value", "@text/fid-absolute-set-value-text"));
        mapDescObj.put("0x0012", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-night", "@text/fid-night-text"));
        mapDescObj.put("0x0013", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-invalid-string-id", "@text/fid-invalid-string-id-text"));
        mapDescObj.put("0x0015", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-rgb-color", "@text/fid-rgb-color-text"));
        mapDescObj.put("0x0016", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-color-temperature", "@text/fid-color-temperature-text"));
        mapDescObj.put("0x0017", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-hsv", "@text/fid-hsv-text"));
        mapDescObj.put("0x0018", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-hue", "@text/fid-hue-text"));
        mapDescObj.put("0x0019", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-saturation", "@text/fid-saturation-text"));
        mapDescObj.put("0x0020", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "@text/fid-move-up-down", "@text/fid-move-up-down-text"));
        mapDescObj.put("0x0021", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "@text/fid-adjust-up-down", "@text/fid-adjust-up-down-text"));
        mapDescObj.put("0x0023", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "@text/fid-set-absolute-position-blinds", "@text/fid-set-absolute-position-blinds-text"));
        mapDescObj.put("0x0024", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "@text/fid-set-absolute-position-slats", "@text/fid-set-absolute-position-slats-text"));
        mapDescObj.put("0x0025", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_WIND, "", "",
                "@text/fid-wind-alarm", "@text/fid-wind-alarm-text"));
        mapDescObj.put("0x0026", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-frost-alarm", "@text/fid-frost-alarm-text"));
        mapDescObj.put("0x0027", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_RAIN, "", "",
                "@text/fid-rain-alarm", "@text/fid-rain-alarm-text"));
        mapDescObj.put("0x0028", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-force-position-blind", "@text/fid-force-position-blind-text"));
        mapDescObj.put("0x0029", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-window-door-position", "@text/fid-window-door-position-text"));
        mapDescObj.put("0x0030", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-actuating-value-heating", "@text/fid-actuating-value-heating-text"));
        mapDescObj.put("0x0031", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-fan-level-heating", "@text/fid-fan-level-heating-text"));
        mapDescObj.put("0x0032", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-actuating-value-cooling", "@text/fid-actuating-value-cooling-text"));
        mapDescObj.put("0x0033", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "@text/fid-set-value-temperature", "@text/fid-set-value-temperature-text"));
        mapDescObj.put("0x0034", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "@text/fid-relative-set-point-temperature", "@text/fid-relative-set-point-temperature-text"));
        mapDescObj.put("0x0035", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-window-door", "@text/fid-window-door-text"));
        mapDescObj.put("0x0036", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-status-indication", "@text/fid-status-indication-text"));
        mapDescObj.put("0x0037", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-fan-manual-heating-on-off", "@text/fid-fan-manual-heating-on-off-text"));
        mapDescObj.put("0x0038", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-controller-on-off", "@text/fid-controller-on-off-text"));
        mapDescObj.put("0x0039", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "@text/fid-relative-set-point-request", "@text/fid-relative-set-point-request-text"));
        mapDescObj.put("0x003A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-eco-mode-on-off-request", "@text/fid-eco-mode-on-off-request-text"));
        mapDescObj.put("0x003B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "@text/fid-comfort-temperature", "@text/fid-comfort-temperature-text"));
        mapDescObj.put("0x0040", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-fan-level-request", "@text/fid-fan-level-request-text"));
        mapDescObj.put("0x0041", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-fan-manual-on-off-request", "@text/fid-fan-manual-on-off-request-text"));
        mapDescObj.put("0x0042", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-controller-on-off-request", "@text/fid-controller-on-off-request-text"));
        mapDescObj.put("0x0044", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-eco-mode-on-off-request", "@text/fid-eco-mode-on-off-request-text"));
        mapDescObj.put("0x0100", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-info-on-off", "@text/fid-info-on-off-text"));
        mapDescObj.put("0x0101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-force-position-info", "@text/fid-force-position-info-text"));
        mapDescObj.put("0x0105", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-infoonoff", "@text/fid-sysap-infoonoff-text"));
        mapDescObj.put("0x0106", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-infoforce", "@text/fid-sysap-infoforce-text"));
        mapDescObj.put("0x0110", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-actual-dimming-value", "@text/fid-info-actual-dimming-value-text"));
        mapDescObj.put("0x0111", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-error", "@text/fid-info-error-text"));
        mapDescObj.put("0x0115", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-infocurrentdimmingvalue", "@text/fid-sysap-infocurrentdimmingvalue-text"));
        mapDescObj.put("0x0116", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-infoerror", "@text/fid-sysap-infoerror-text"));
        mapDescObj.put("0x0118", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-color-temperature", "@text/fid-info-color-temperature-text"));
        mapDescObj.put("0x011A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-info-color-temperature", "@text/fid-sysap-info-color-temperature-text"));
        mapDescObj.put("0x011B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-hsv", "@text/fid-info-hsv-text"));
        mapDescObj.put("0x011C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-info-hsv", "@text/fid-sysap-info-hsv-text"));
        mapDescObj.put("0x011D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-color-mode", "@text/fid-info-color-mode-text"));
        mapDescObj.put("0x011E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-info-color-mode", "@text/fid-sysap-info-color-mode-text"));
        mapDescObj.put("0x0120", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "@text/fid-info-move-up-down", "@text/fid-info-move-up-down-text"));
        mapDescObj.put("0x0121",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "", "",
                        "@text/fid-current-absolute-position-blinds-percentage",
                        "@text/fid-current-absolute-position-blinds-percentage-text"));
        mapDescObj.put("0x0122",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "@text/fid-current-absolute-position-slats-percentage",
                        "@text/fid-current-absolute-position-slats-percentage-text"));
        mapDescObj.put("0x0125", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-infomoveupdown", "@text/fid-sysap-infomoveupdown-text"));
        mapDescObj.put("0x0126",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "@text/fid-sysap-infocurrentabsoluteblindspercentage",
                        "@text/fid-sysap-infocurrentabsoluteblindspercentage-text"));
        mapDescObj.put("0x0127",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "@text/fid-sysap-infocurrentabsoluteslatspercentage",
                        "@text/fid-sysap-infocurrentabsoluteslatspercentage-text"));
        mapDescObj.put("0x0130", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "@text/fid-measured-temperature", "@text/fid-measured-temperature-text"));
        mapDescObj.put("0x0131", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-value-heating", "@text/fid-info-value-heating-text"));
        mapDescObj.put("0x0132", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-info-value-cooling", "@text/fid-info-value-cooling-text"));
        mapDescObj.put("0x0135", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-switchover-heating-cooling", "@text/fid-switchover-heating-cooling-text"));
        mapDescObj.put("0x0136", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-actuating-fan-stage-heating", "@text/fid-actuating-fan-stage-heating-text"));
        mapDescObj.put("0x0140", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "@text/fid-absolute-setpoint-temperature", "@text/fid-absolute-setpoint-temperature-text"));
        mapDescObj.put("0x0141", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-additional-heating-value-info", "@text/fid-additional-heating-value-info-text"));
        mapDescObj.put("0x0142", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-additional-cooling-value-info", "@text/fid-additional-cooling-value-info-text"));
        mapDescObj.put("0x0143", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-control-value-additional-heating", "@text/fid-control-value-additional-heating-text"));
        mapDescObj.put("0x0144", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-control-value-additional-cooling", "@text/fid-control-value-additional-cooling-text"));
        mapDescObj.put("0x0145", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-actuating-fan-stage-heating", "@text/fid-info-actuating-fan-stage-heating-text"));
        mapDescObj.put("0x0146",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "@text/fid-info-actuating-fan-manual-on-off-heating",
                        "@text/fid-info-actuating-fan-manual-on-off-heating-text"));
        mapDescObj.put("0x0147", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-actuating-fan-stage-cooling", "@text/fid-actuating-fan-stage-cooling-text"));
        mapDescObj.put("0x0149", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-info-fan-stage-cooling", "@text/fid-info-fan-stage-cooling-text"));
        mapDescObj.put("0x014A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-info-fan-manual-on-off-cooling", "@text/fid-info-fan-manual-on-off-cooling-text"));
        mapDescObj.put("0x014B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-heating-active", "@text/fid-heating-active-text"));
        mapDescObj.put("0x014C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-cooling-active", "@text/fid-cooling-active-text"));
        mapDescObj.put("0x014D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-heating-demand", "@text/fid-heating-demand-text"));
        mapDescObj.put("0x014E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-cooling-demand", "@text/fid-cooling-demand-text"));
        mapDescObj.put("0x014F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-heating-demand-feedback-signal", "@text/fid-heating-demand-feedback-signal-text"));
        mapDescObj.put("0x0150", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-cooling-demand-feedback-signal", "@text/fid-cooling-demand-feedback-signal-text"));
        mapDescObj.put("0x0151", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_HUMIDITY, "", "",
                "@text/fid-humidity", "@text/fid-humidity-text"));
        mapDescObj.put("0x0152", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-aux-on-off-request", "@text/fid-aux-on-off-request-text"));
        mapDescObj.put("0x0153", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-aux-on-off-response", "@text/fid-aux-on-off-response-text"));
        mapDescObj.put("0x0154", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-heating-on-off-request", "@text/fid-heating-on-off-request-text"));
        mapDescObj.put("0x0155", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-cooling-on-off-request", "@text/fid-cooling-on-off-request-text"));
        mapDescObj.put("0x0156", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-operation-mode", "@text/fid-operation-mode-text"));
        mapDescObj.put("0x0157", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-swing-h-v", "@text/fid-swing-h-v-text"));
        mapDescObj.put("0x0158", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-supported-features", "@text/fid-supported-features-text"));
        mapDescObj.put("0x0159", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-extended-status-indication", "@text/fid-extended-status-indication-text"));
        mapDescObj.put("0x015A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-extended-status-indication", "@text/fid-extended-status-indication-text"));
        mapDescObj.put("0x015B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-aux-heating-on-off-request", "@text/fid-aux-heating-on-off-request-text"));
        mapDescObj.put("0x015C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-emergency-heating-on-off-request", "@text/fid-emergency-heating-on-off-request-text"));
        mapDescObj.put("0x0160", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-relative-fan-speed-control", "@text/fid-relative-fan-speed-control-text"));
        mapDescObj.put("0x0161", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-absolute-fan-speed-control", "@text/fid-absolute-fan-speed-control-text"));
        mapDescObj.put("0x0162", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-absolute-fan-speed", "@text/fid-info-absolute-fan-speed-text"));
        mapDescObj.put("0x0163", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-sysap-infoactualfanspeed", "@text/fid-sysap-infoactualfanspeed-text"));
        mapDescObj.put("0x01A0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-notification-flags", "@text/fid-notification-flags-text"));
        mapDescObj.put("0x0280", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-power-rc", "@text/fid-power-rc-text"));
        mapDescObj.put("0x0281", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-power-rh", "@text/fid-power-rh-text"));
        mapDescObj.put("0x0282", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-proximity-status", "@text/fid-proximity-status-text"));
        mapDescObj.put("0x0290", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-brightness-sensor", "@text/fid-brightness-sensor-text"));
        mapDescObj.put("0x0291", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-last-touch", "@text/fid-last-touch-text"));
        mapDescObj.put("0x0292", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-led-backlighting-night-mode", "@text/fid-led-backlighting-night-mode-text"));
        mapDescObj.put("0x02C0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-locator-beep", "@text/fid-locator-beep-text"));
        mapDescObj.put("0x02C1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-switch-test-alarm", "@text/fid-switch-test-alarm-text"));
        mapDescObj.put("0x02C3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-fire-alarm-active", "@text/fid-fire-alarm-active-text"));
        mapDescObj.put("0x0400", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "",
                "", "@text/fid-outside-temperature", "@text/fid-outside-temperature-text"));
        mapDescObj.put("0x0401", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_WIND, "", "",
                "@text/fid-wind-force", "@text/fid-wind-force-text"));
        mapDescObj.put("0x0402", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-brightness-alarm", "@text/fid-brightness-alarm-text"));
        mapDescObj.put("0x0403", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_UNDEFINED, "", "",
                "@text/fid-lux-value", "@text/fid-lux-value-text"));
        mapDescObj.put("0x0404", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_WIND, "", "",
                "@text/fid-wind-speed", "@text/fid-wind-speed-text"));
        mapDescObj.put("0x0405", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_RAIN, "", "",
                "@text/fid-rain-detection", "@text/fid-rain-detection-text"));
        mapDescObj.put("0x0406", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_RAIN, "", "",
                "@text/fid-rain-sensor-frequency", "@text/fid-rain-sensor-frequency-text"));
        mapDescObj.put("0x0440", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-play", "@text/fid-play-text"));
        mapDescObj.put("0x0441", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-pause", "@text/fid-pause-text"));
        mapDescObj.put("0x0442", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-next", "@text/fid-next-text"));
        mapDescObj.put("0x0443", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-previous", "@text/fid-previous-text"));
        mapDescObj.put("0x0444", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-play-mode", "@text/fid-play-mode-text"));
        mapDescObj.put("0x0445", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-mute", "@text/fid-mute-text"));
        mapDescObj.put("0x0446", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-relative-volume-control", "@text/fid-relative-volume-control-text"));
        mapDescObj.put("0x0447", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-absolute-volume-control", "@text/fid-absolute-volume-control-text"));
        mapDescObj.put("0x0448", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-group-membership", "@text/fid-group-membership-text"));
        mapDescObj.put("0x0449", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-play-favorite", "@text/fid-play-favorite-text"));
        mapDescObj.put("0x044A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-play-next-favorite", "@text/fid-play-next-favorite-text"));
        mapDescObj.put("0x0460", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-playback-status", "@text/fid-playback-status-text"));
        mapDescObj.put("0x0461", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-current-item-metadata-info", "@text/fid-current-item-metadata-info-text"));
        mapDescObj.put("0x0462", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-mute", "@text/fid-info-mute-text"));
        mapDescObj.put("0x0463", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-actual-volume", "@text/fid-info-actual-volume-text"));
        mapDescObj.put("0x0464", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-allowed-playback-actions", "@text/fid-allowed-playback-actions-text"));
        mapDescObj.put("0x0465", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-group-membership", "@text/fid-info-group-membership-text"));
        mapDescObj.put("0x0466", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-playing-favorite", "@text/fid-info-playing-favorite-text"));
        mapDescObj.put("0x0467", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-absolute-group-volume-control", "@text/fid-absolute-group-volume-control-text"));
        mapDescObj.put("0x0468", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-absolute-group-volume", "@text/fid-info-absolute-group-volume-text"));
        mapDescObj.put("0x0469", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-media-source", "@text/fid-media-source-text"));
        mapDescObj.put("0x04A0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-solar-power-production", "@text/fid-solar-power-production-text"));
        mapDescObj.put("0x04A1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-inverter-output-power", "@text/fid-inverter-output-power-text"));
        mapDescObj.put("0x04A2", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-solar-energy-(today)", "@text/fid-solar-energy-(today)-text"));
        mapDescObj.put("0x04A3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-injected-energy-(today)", "@text/fid-injected-energy-(today)-text"));
        mapDescObj.put("0x04A4", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-purchased-energy-(today)", "@text/fid-purchased-energy-(today)-text"));
        mapDescObj.put("0x04A5", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-inverter-alarm", "@text/fid-inverter-alarm-text"));
        mapDescObj.put("0x04A6", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-self-consumption", "@text/fid-self-consumption-text"));
        mapDescObj.put("0x04A7", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-self-sufficiency", "@text/fid-self-sufficiency-text"));
        mapDescObj.put("0x04A8", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-home-power-consumption", "@text/fid-home-power-consumption-text"));
        mapDescObj.put("0x04A9", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-power-to-grid", "@text/fid-power-to-grid-text"));
        mapDescObj.put("0x04AA", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-consumed-energy-(today)", "@text/fid-consumed-energy-(today)-text"));
        mapDescObj.put("0x04AB", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-meter-alarm", "@text/fid-meter-alarm-text"));
        mapDescObj.put("0x04AC", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-battery-level", "@text/fid-battery-level-text"));
        mapDescObj.put("0x04AD", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-battery-power", "@text/fid-battery-power-text"));
        mapDescObj.put("0x04B0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-boost", "@text/fid-boost-text"));
        mapDescObj.put("0x04B1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-stop-charging-reuqest", "@text/fid-stop-charging-reuqest-text"));
        mapDescObj.put("0x04B2", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-enable-charging-reuqest", "@text/fid-enable-charging-reuqest-text"));
        mapDescObj.put("0x04B3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-boost", "@text/fid-info-boost-text"));
        mapDescObj.put("0x04B4", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-wallbox-status", "@text/fid-info-wallbox-status-text"));
        mapDescObj.put("0x04B5", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-charging", "@text/fid-info-charging-text"));
        mapDescObj.put("0x04B6", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-charging-enabled", "@text/fid-info-charging-enabled-text"));
        mapDescObj.put("0x04B7", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-installed-power", "@text/fid-info-installed-power-text"));
        mapDescObj.put("0x04B8", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-transmitted-energy", "@text/fid-info-transmitted-energy-text"));
        mapDescObj.put("0x04B9", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-car-range", "@text/fid-info-car-range-text"));
        mapDescObj.put("0x04BA", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-charging-duration", "@text/fid-info-charging-duration-text"));
        mapDescObj.put("0x04BB", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-current-limit", "@text/fid-info-current-limit-text"));
        mapDescObj.put("0x04BC", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-current-limit-for-group", "@text/fid-info-current-limit-for-group-text"));
        mapDescObj.put("0x04BD", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-album-cover-url", "@text/fid-album-cover-url-text"));
        mapDescObj.put("0x0501", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-secure@home-central-unit", "@text/fid-secure@home-central-unit-text"));
        mapDescObj.put("0x0502", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-domusdisarmcounter", "@text/fid-domusdisarmcounter-text"));
        mapDescObj.put("0x0504", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-intrusion-alarm", "@text/fid-intrusion-alarm-text"));
        mapDescObj.put("0x0505", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-safety-alarm", "@text/fid-safety-alarm-text"));
        mapDescObj.put("0x0507", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-infoconfigurationstatus", "@text/fid-infoconfigurationstatus-text"));
        mapDescObj.put("0x0508", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-enable-configuration", "@text/fid-enable-configuration-text"));
        mapDescObj.put("0x0509", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-disarming-led", "@text/fid-disarming-led-text"));
        mapDescObj.put("0x050A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-aes-key", "@text/fid-aes-key-text"));
        mapDescObj.put("0x050B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-zone-status", "@text/fid-zone-status-text"));
        mapDescObj.put("0x050E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-time", "@text/fid-time-text"));
        mapDescObj.put("0x0600", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-start-stop", "@text/fid-start-stop-text"));
        mapDescObj.put("0x0601", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-pause-resume", "@text/fid-pause-resume-text"));
        mapDescObj.put("0x0602", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-select-program", "@text/fid-select-program-text"));
        mapDescObj.put("0x0603", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-delayed-start-time", "@text/fid-delayed-start-time-text"));
        mapDescObj.put("0x0604", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-status", "@text/fid-info-status-text"));
        mapDescObj.put("0x0605", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-remote-start-enabled", "@text/fid-info-remote-start-enabled-text"));
        mapDescObj.put("0x0606", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-program", "@text/fid-info-program-text"));
        mapDescObj.put("0x0607", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-finish-time", "@text/fid-info-finish-time-text"));
        mapDescObj.put("0x0608", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-delayed-start", "@text/fid-info-delayed-start-text"));
        mapDescObj.put("0x0609", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-door", "@text/fid-info-door-text"));
        mapDescObj.put("0x060A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-door-alarm", "@text/fid-info-door-alarm-text"));
        mapDescObj.put("0x060B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-switch-supercool", "@text/fid-switch-supercool-text"));
        mapDescObj.put("0x060C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-switch-superfreeze", "@text/fid-switch-superfreeze-text"));
        mapDescObj.put("0x060D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-switch-supercool", "@text/fid-info-switch-supercool-text"));
        mapDescObj.put("0x060E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-switch-superfreeze", "@text/fid-info-switch-superfreeze-text"));
        mapDescObj.put("0x060F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-measured-temperature", "@text/fid-measured-temperature-text"));
        mapDescObj.put("0x0610", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-measured-temperature", "@text/fid-measured-temperature-text"));
        mapDescObj.put("0x0611", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-set-value-temperature", "@text/fid-set-value-temperature-text"));
        mapDescObj.put("0x0612", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-set-value-temperature", "@text/fid-set-value-temperature-text"));
        mapDescObj.put("0x0613", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-change-operation", "@text/fid-change-operation-text"));
        mapDescObj.put("0x0614", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-detailed-status-info", "@text/fid-detailed-status-info-text"));
        mapDescObj.put("0x0615", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-remaining-time", "@text/fid-info-remaining-time-text"));
        mapDescObj.put("0x0616", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-time-of-last-status-change-(star", "@text/fid-time-of-last-status-change-(star-text"));
        mapDescObj.put("0x0618", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-lock-unlock-door-command", "@text/fid-lock-unlock-door-command-text"));
        mapDescObj.put("0x0619", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-locked-unlocked", "@text/fid-info-locked-unlocked-text"));
        mapDescObj.put("0xF001", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-time", "@text/fid-time-text"));
        mapDescObj.put("0xF002", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-date", "@text/fid-date-text"));
        mapDescObj.put("0xF003", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-notification", "@text/fid-notification-text"));
        mapDescObj.put("0xF101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-switch-entity-on-off", "@text/fid-switch-entity-on-off-text"));
        mapDescObj.put("0xF102", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "@text/fid-info-switch-entity-on-off", "@text/fid-info-switch-entity-on-off-text"));
        mapDescObj.put("0xF104", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-consistency-tag", "@text/fid-consistency-tag-text"));
        mapDescObj.put("0xF105", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-battery-status", "@text/fid-battery-status-text"));
        mapDescObj.put("0xF106", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-stay-awake", "@text/fid-stay-awake-text"));
        mapDescObj.put("0xF107", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-proxy-switch", "@text/fid-proxy-switch-text"));
        mapDescObj.put("0xF108", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-proxy1", "@text/fid-proxy1-text"));
        mapDescObj.put("0xF109", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-proxy2", "@text/fid-proxy2-text"));
        mapDescObj.put("0xF10A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-proxy4", "@text/fid-proxy4-text"));
        mapDescObj.put("0xF10B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-cyclic-sleep-time", "@text/fid-cyclic-sleep-time-text"));
        mapDescObj.put("0xF10C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-presence", "@text/fid-presence-text"));
        mapDescObj.put("0xF10D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-measured-temperature-1", "@text/fid-measured-temperature-1-text"));
        mapDescObj.put("0xF10E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-standby-statistics", "@text/fid-standby-statistics-text"));
        mapDescObj.put("0xF10F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-heartbeat-delay", "@text/fid-heartbeat-delay-text"));
        mapDescObj.put("0xF110", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-info-heartbeat-delay", "@text/fid-info-heartbeat-delay-text"));
        mapDescObj.put("0xFF01", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-measured-temperature-1", "@text/fid-measured-temperature-1-text"));
        mapDescObj.put("0xFF02", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-measured-temperature-2", "@text/fid-measured-temperature-2-text"));
        mapDescObj.put("0xFF03", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-measured-temperature-3", "@text/fid-measured-temperature-3-text"));
        mapDescObj.put("0xFF04", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "@text/fid-measured-temperature-4", "@text/fid-measured-temperature-4-text"));
        mapDescObj.put("0x061A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_PRESSURE, "", "",
                "@text/fid-air-quality-pressure-value", "@text/fid-air-quality-pressure-value-text"));
        mapDescObj.put("0x061B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-air-quality-co2-value", "@text/fid-air-quality-co2-value-text"));
        mapDescObj.put("0x061C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-air-quality-co-value", "@text/fid-air-quality-co-value-text"));
        mapDescObj.put("0x061D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-air-quality-no2-value", "@text/fid-air-quality-no2-value-text"));
        mapDescObj.put("0x061E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-air-quality-o3-value", "@text/fid-air-quality-o3-value-text"));
        mapDescObj.put("0x061F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-air-quality-pm10-value", "@text/fid-air-quality-pm10-value-text"));
        mapDescObj.put("0x0620", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-air-quality-pm25-value", "@text/fid-air-quality-pm25-value-text"));
        mapDescObj.put("0x0621", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "@text/fid-air-quality-voc-value", "@text/fid-air-quality-voc-value-text"));

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
