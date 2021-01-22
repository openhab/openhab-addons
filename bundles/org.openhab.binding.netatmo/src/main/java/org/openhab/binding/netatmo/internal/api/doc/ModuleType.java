/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.doc;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This enum all handled Netatmo modules and devices along with their capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    // Security Group
    NAHomeSecurity(List.of(GROUP_HOME_SECURITY), null, RefreshPolicy.CONFIG, null),
    NAPerson(List.of(GROUP_PERSON, GROUP_PERSON_EVENT), null, RefreshPolicy.PARENT, NAHomeSecurity),
    NACamera(List.of(GROUP_WELCOME, GROUP_WELCOME_EVENT), null, RefreshPolicy.PARENT, NAHomeSecurity),
    NOC(List.of(GROUP_WELCOME, GROUP_WELCOME_EVENT, GROUP_PRESENCE), null, RefreshPolicy.PARENT, NAHomeSecurity),

    // Weather group
    NAMain(List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_CO2, GROUP_NOISE, GROUP_PRESSURE, GROUP_DEVICE,
            GROUP_SIGNAL), List.of("measure", "measure-timestamp"), RefreshPolicy.AUTO, null),
    NAModule1(List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY),
            List.of("measure", "measure-timestamp"), RefreshPolicy.PARENT, NAMain),
    NAModule2(List.of(GROUP_WIND, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY), null, RefreshPolicy.PARENT, NAMain),
    NAModule3(List.of(GROUP_RAIN, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY), List.of("sum-rain"), RefreshPolicy.PARENT,
            NAMain),
    NAModule4(List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_CO2, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY),
            List.of("measure", "measure-timestamp"), RefreshPolicy.PARENT, NAMain),

    // Aircare group
    NHC(List.of(GROUP_HEALTH, GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_PRESSURE, GROUP_CO2, GROUP_NOISE, GROUP_DEVICE,
            GROUP_SIGNAL), List.of("measure", "measure-timestamp"), RefreshPolicy.AUTO, null),

    // Energy group
    NAHomeEnergy(List.of(GROUP_HOME_ENERGY), null, RefreshPolicy.CONFIG, null),
    NAPlug(List.of(GROUP_PLUG, GROUP_DEVICE, GROUP_SIGNAL), null, RefreshPolicy.CONFIG, NAHomeEnergy),
    NATherm1(List.of(GROUP_TH_PROPERTIES, GROUP_TH_SETPOINT, GROUP_TH_TEMPERATURE, GROUP_MODULE, GROUP_SIGNAL,
            GROUP_BATTERY), null, RefreshPolicy.PARENT, NAPlug),

    // Left for future implementation
    // NACamDoorTag,
    // NSD,
    // NIS,
    // NDB
    ;

    public enum RefreshPolicy {
        AUTO,
        PARENT,
        CONFIG;
    }

    public final List<String> groups;
    public final @Nullable List<String> extensions;
    public RefreshPolicy refreshPeriod;
    public final @Nullable ThingTypeUID bridgeThingType;
    public final ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, this.name());

    ModuleType(List<String> groups, @Nullable List<String> extensions, RefreshPolicy refreshPeriod,
            @Nullable ModuleType bridge) {
        this.groups = groups;
        this.refreshPeriod = refreshPeriod;
        this.extensions = extensions;
        this.refreshPeriod = refreshPeriod;
        this.bridgeThingType = bridge != null ? bridge.thingTypeUID : null;
    }

    public boolean matches(ThingTypeUID otherThingTypeUID) {
        return thingTypeUID.equals(otherThingTypeUID);
    }

    public boolean hasBattery() {
        return groups.contains(GROUP_BATTERY);
    }

    public int[] getSignalLevels() {
        return groups.contains(GROUP_SIGNAL) ? (hasBattery() ? RADIO_SIGNAL_LEVELS : WIFI_SIGNAL_LEVELS) : NO_RADIO;
    }
}
