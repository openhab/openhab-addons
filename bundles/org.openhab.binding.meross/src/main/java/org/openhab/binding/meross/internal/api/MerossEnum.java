/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.api;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MerossEnum} class is responsible for defining enum constants for the whole binding.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossEnum {
    public enum HttpEndpoint {
        LOGIN("/v1/Auth/signIn"),
        LOGOUT("/v1/Profile/logout"),
        DEV_LIST("/v1/Device/devList");

        public String value() {
            return value;
        }

        private final String value;

        HttpEndpoint(String value) {
            this.value = value;
        }
    }

    public enum Namespace {
        // Common abilities
        SYSTEM_ALL("Appliance.System.All"),
        SYSTEM_ABILITY("Appliance.System.Ability"),
        SYSTEM_ONLINE("Appliance.System.Online"),
        SYSTEM_REPORT("Appliance.System.Report"),
        SYSTEM_DEBUG("Appliance.System.Debug"),
        SYSTEM_RUNTIME("Appliance.System.Runtime"),
        SYSTEM_ENCRYPTION("Appliance.Encrypt.Suite"),
        SYSTEM_ENCRYPTION_ECDHE("Appliance.Encrypt.ECDHE"),
        CONTROL_BIND("Appliance.Control.Bind"),
        CONTROL_CONTROL_UNBIND("Appliance.Control.Unbind"),
        CONTROL_TRIGGER("Appliance.Control.Trigger"),
        CONTROL_TRIGGERX("Appliance.Control.TriggerX"),
        CONFIG_WIFI_LIST("Appliance.Config.WifiList"),
        CONFIG_TRACE("Appliance.Config.Trace"),
        SYSTEM_DND_MODE("Appliance.System.DND.Mode"),

        // Power bulb/plug capabilities

        CONTROL_TOGGLE("Appliance.Control.Toggle"),
        CONTROL_TOGGLEX("Appliance.Control.ToggleX"),
        CONTROL_ELECTRICITY("Appliance.Control.Electricity"),
        CONTROL_CONSUMPTION("Appliance.Control.Consumption"),
        CONTROL_CONSUMPTIONX("Appliance.Control.ConsumptionX"),

        // Bulb only abilities

        CONTROL_LIGHT("Appliance.Control.Light"),

        // Garage opener abilities

        GARAGE_DOOR_STATE("Appliance.GarageDoor.State"),
        GARAGE_DOOR_MULTIPLE_CONFIG("Appliance.GarageDoor.MultipleConfig"),

        // Roller shutter timer

        ROLLER_SHUTTER_STATE("Appliance.RollerShutter.State"),
        ROLLER_SHUTTER_POSITION("Appliance.RollerShutter.Position"),
        ROLLER_SHUTTER_CONFIG("Appliance.RollerShutter.Config"),

        // Humidifier

        CONTROL_SPRAY("Appliance.Control.Spray"),
        SYSTEM_DIGEST_HUB("Appliance.System.Digest.Hub"),

        // Oil diffuser

        DIFFUSER_LIGHT("Appliance.Control.Diffuser.Light"),
        DIFFUSER_SPRAY("Appliance.Control.Diffuser.Spray"),

        // Hub

        HUB_EXCEPTION("Appliance.Hub.Exception"),
        HUB_BATTERY("Appliance.Hub.Battery"),
        HUB_TOGGLEX("Appliance.Hub.ToggleX"),
        HUB_ONLINE("Appliance.Hub.Online"),
        HUB_SUBDEVICE_LIST("Appliance.Hub.SubdeviceList"),

        // Sensors

        HUB_SENSOR_ALL("Appliance.Hub.Sensor.All"),
        HUB_SENSOR_TEMPHUM("Appliance.Hub.Sensor.TempHum"),
        HUB_SENSOR_ALERT("Appliance.Hub.Sensor.Alert"),

        // MTS 100

        HUB_MTS100_ALL("Appliance.Hub.Mts100.All"),
        HUB_MTS100_TEMPERATURE("Appliance.Hub.Mts100.Temperature"),
        HUB_MTS100_MODE("Appliance.Hub.Mts100.Mode"),
        HUB_MTS100_ADJUST("Appliance.Hub.Mts100.Adjust"),

        // Thermostat / MTS200

        CONTROL_THERMOSTAT_MODE("Appliance.Control.Thermostat.Mode"),
        CONTROL_THERMOSTAT_WINDOWOPENED("Appliance.Control.Thermostat.WindowOpened");

        public String value() {
            return value;
        }

        private final String value;

        Namespace(String value) {
            this.value = value;
        }

        public static @Nullable String getAbilityValueByName(String name) {
            return Stream.of(Namespace.values()).filter(p -> p.name().equals(name)).map(Namespace::value).findFirst()
                    .orElse("Unidentified Ability");
        }
    }

    public enum ApiStatusCode {
        OK(0),
        WRONG_OR_MISSING_USER(1000),
        WRONG_OR_MISSING_PASSWORD(1001),
        ACCOUNT_DOES_NOT_EXIST(1002),
        THIS_ACCOUNT_HAS_BEEN_DISABLED_OR_DELETED(1003),
        WRONG_EMAIL_OR_PASSWORD(1004),
        INVALID_EMAIL_ADDRESS(1005),
        BAD_PASSWORD_FORMAT(1006),
        USER_ALREADY_EXISTS(1007),
        THIS_EMAIL_IS_NOT_REGISTERED(1008),
        SEND_EMAIL_FAILED(1009),
        WRONG_TICKET(1011),
        CODE_TOKEN_ERROR(1022),
        TOO_MANY_TOKENS(1301);

        public int value() {
            return value;
        }

        private final int value;

        ApiStatusCode(int value) {
            this.value = value;
        }

        public static @Nullable String getMessageByApiStatusCode(int statusCode) {
            return Stream.of(ApiStatusCode.values()).filter(s -> s.value() == statusCode).map(ApiStatusCode::name)
                    .findFirst().orElse("Unidentified Api Status Message");
        }
    }

    public enum OnlineStatus {
        NOT_ONLINE(0),
        ONLINE(1),
        OFFLINE(2),
        UNKNOWN(-1),
        UPGRADING(3);

        final int value;

        OnlineStatus(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }
}
