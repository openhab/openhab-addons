/**
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
package org.openhab.binding.ferroamp.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FerroampBindingConstants} class defines common constants, which are
 * used throughout the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */
@NonNullByDefault
public class FerroampBindingConstants {

    public static final String BINDING_ID = "ferroamp";

    // Broker (energyhub) port number
    static final int BROKER_PORT = 1883;

    // Broker (energyhub) status
    public static final String CONNECTED = "connected";

    // Broker (energyhub) topics
    public static final String EHUB_TOPIC = "extapi/data/ehub";
    public static final String SSO_TOPIC = "extapi/data/sso";
    public static final String ESO_TOPIC = "extapi/data/eso";
    public static final String ESM_TOPIC = "extapi/data/esm";
    public static final String REQUEST_TOPIC = "extapi/control/request";

    // Broker (energyhub) QOS level
    public static final String QOS = "2";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGYHUB = new ThingTypeUID(BINDING_ID, "energyhub");

    // List of EHUB Channel ids
    public static final String CHANNEL_GRIDFREQUENCY = "grid-frequency";
    public static final String CHANNEL_ACECURRENTL1 = "ace-current-l1";
    public static final String CHANNEL_ACECURRENTL2 = "ace-current-l2";
    public static final String CHANNEL_ACECURRENTL3 = "ace-current-l3";
    public static final String CHANNEL_GRIDVOLTAGEL1 = "grid-voltage-l1";
    public static final String CHANNEL_GRIDVOLTAGEL2 = "grid-voltage-l2";
    public static final String CHANNEL_GRIDVOLTAGEL3 = "grid-voltage-l3";
    public static final String CHANNEL_INVERTERRMSCURRENTL1 = "inverter-rms-current-l1";
    public static final String CHANNEL_INVERTERRMSCURRENTL2 = "inverter-rms-current-l2";
    public static final String CHANNEL_INVERTERRMSCURRENTL3 = "inverter-rms-current-l3";
    public static final String CHANNEL_INVERTERREACTIVECURRENTL1 = "inverter-reactive-current-l1";
    public static final String CHANNEL_INVERTERREACTIVECURRENTL2 = "inverter-reactive-current-l2";
    public static final String CHANNEL_INVERTERREACTIVECURRENTL3 = "inverter-reactive-current-l3";
    public static final String CHANNEL_INVERTERACTIVECURRENTL1 = "inverter-active-current-l1";
    public static final String CHANNEL_INVERTERACTIVECURRENTL2 = "inverter-active-current-l2";
    public static final String CHANNEL_INVERTERACTIVECURRENTL3 = "inverter-active-current-l3";
    public static final String CHANNEL_GRIDCURRENTL1 = "grid-current-l1";
    public static final String CHANNEL_GRIDCURRENTL2 = "grid-current-l2";
    public static final String CHANNEL_GRIDCURRENTL3 = "grid-current-l3";
    public static final String CHANNEL_GRIDREACTIVECURRENTL1 = "grid-reactive-current-l1";
    public static final String CHANNEL_GRIDREACTIVECURRENTL2 = "grid-reactive-current-l2";
    public static final String CHANNEL_GRIDREACTIVECURRENTL3 = "grid-reactive-current-l3";
    public static final String CHANNEL_GRIDACTIVECURRENTL1 = "grid-active-current-l1";
    public static final String CHANNEL_GRIDACTIVECURRENTL2 = "grid-active-current-l2";
    public static final String CHANNEL_GRIDACTIVECURRENTL3 = "grid-active-current-l3";
    public static final String CHANNEL_INVERTERLOADREACTIVECURRENTL1 = "inverter-load-reactive-current-l1";
    public static final String CHANNEL_INVERTERLOADREACTIVECURRENTL2 = "inverter-load-reactive-current-l2";
    public static final String CHANNEL_INVERTERLOADREACTIVECURRENTL3 = "inverter-load-reactive-current-l3";
    public static final String CHANNEL_INVERTERLOADACTIVECURRENTL1 = "inverter-load-active-current-l1";
    public static final String CHANNEL_INVERTERLOADACTIVECURRENTL2 = "inverter-load-active-current-l2";
    public static final String CHANNEL_INVERTERLOADACTIVECURRENTL3 = "inverter-load-active-current-l3";
    public static final String CHANNEL_APPARENTPOWER = "apparent-power";
    public static final String CHANNEL_GRIDPOWERACTIVEL1 = "grid-power-active-l1";
    public static final String CHANNEL_GRIDPOWERACTIVEL2 = "grid-power-active-l2";
    public static final String CHANNEL_GRIDPOWERACTIVEL3 = "grid-power-active-l3";
    public static final String CHANNEL_GRIDPOWERREACTIVEL1 = "grid-power-reactive-l1";
    public static final String CHANNEL_GRIDPOWERREACTIVEL2 = "grid-power-reactive-l2";
    public static final String CHANNEL_GRIDPOWERREACTIVEL3 = "grid-power-reactive-l3";
    public static final String CHANNEL_INVERTERPOWERACTIVEL1 = "inverter-power-active-l1";
    public static final String CHANNEL_INVERTERPOWERACTIVEL2 = "inverter-power-active-l2";
    public static final String CHANNEL_INVERTERPOWERACTIVEL3 = "inverter-power-active-l3";
    public static final String CHANNEL_INVERTERPOWERREACTIVEL1 = "inverter-power-reactive-l1";
    public static final String CHANNEL_INVERTERPOWERREACTIVEL2 = "inverter-power-reactive-l2";
    public static final String CHANNEL_INVERTERPOWERREACTIVEL3 = "inverter-power-reactive-l3";
    public static final String CHANNEL_CONSUMPTIONPOWERL1 = "consumption-power-l1";
    public static final String CHANNEL_CONSUMPTIONPOWERL2 = "consumption-power-l2";
    public static final String CHANNEL_CONSUMPTIONPOWERL3 = "consumption-power-l3";
    public static final String CHANNEL_CONSUMPTIONPOWERREACTIVEL1 = "consumption-power-reactive-l1";
    public static final String CHANNEL_CONSUMPTIONPOWERREACTIVEL2 = "consumption-power-reactive-l2";
    public static final String CHANNEL_CONSUMPTIONPOWERREACTIVEL3 = "consumption-power-reactive-l3";
    public static final String CHANNEL_SOLARPV = "solar-pv";
    public static final String CHANNEL_POSITIVEDCLINKVOLTAGE = "positive-dc-link-voltage";
    public static final String CHANNEL_NEGATIVEDCLINKVOLTAGE = "negative-dc-link-voltage";
    public static final String CHANNEL_GRIDENERGYPRODUCEDL1 = "grid-energy-produced-l1";
    public static final String CHANNEL_GRIDENERGYPRODUCEDL2 = "grid-energy-produced-l2";
    public static final String CHANNEL_GRIDENERGYPRODUCEDL3 = "grid-energy-produced-l3";
    public static final String CHANNEL_GRIDENERGYCONSUMEDL1 = "grid-energy-consumed-l1";
    public static final String CHANNEL_GRIDENERGYCONSUMEDL2 = "grid-energy-consumed-l2";
    public static final String CHANNEL_GRIDENERGYCONSUMEDL3 = "grid-energy-consumed-l3";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDL1 = "inverter-energy-produced-l1";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDL2 = "inverter-energy-produced-l2";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDL3 = "inverter-energy-produced-l3";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDL1 = "inverter-energy-consumed-l1";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDL2 = "inverter-energy-consumed-l2";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDL3 = "inverter-energy-consumed-l3";
    public static final String CHANNEL_LOADENERGYPRODUCEDL1 = "load-energy-produced-l1";
    public static final String CHANNEL_LOADENERGYPRODUCEDL2 = "load-energy-produced-l2";
    public static final String CHANNEL_LOADENERGYPRODUCEDL3 = "load-energy-produced-l3";
    public static final String CHANNEL_LOADENERGYCONSUMEDL1 = "load-energy-consumed-l1";
    public static final String CHANNEL_LOADENERGYCONSUMEDL2 = "load-energy-consumed-l2";
    public static final String CHANNEL_LOADENERGYCONSUMEDL3 = "load-energy-consumed-l3";
    public static final String CHANNEL_GRIDENERGYPRODUCEDTOTAL = "grid-energy-produced-total";
    public static final String CHANNEL_GRIDENERGYCONSUMEDTOTAL = "grid-energy-consumed-total";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDTOTAL = "inverter-energy-produced-total";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDTOTAL = "inverter-energy-consumed-total";
    public static final String CHANNEL_LOADENERGYPRODUCEDTOTAL = "load-energy-produced-total";
    public static final String CHANNEL_LOADENERGYCONSUMEDTOTAL = "load-energy-consumed-total";
    public static final String CHANNEL_TOTALSOLARENERGY = "total-solar-energy";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_TIMESTAMP = "timestamp";

    // List of battery setup Channel ids
    public static final String CHANNEL_BATTERYENERGYPRODUCED = "battery-energy-produced";
    public static final String CHANNEL_BATTERYENERGYCONSUMED = "battery-energy-consumed";
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_SOH = "soh";
    public static final String CHANNEL_POWERBATTERY = "power-battery";
    public static final String CHANNEL_TOTALCAPACITYBATTERIES = "total-capacity-batteries";

    // List of SSO Channel ids
    public static final String CHANNEL_S1ID = "s1-id";
    public static final String CHANNEL_S1PVVOLTAGE = "s1-pv-voltage";
    public static final String CHANNEL_S1PVCURRENT = "s1-pv-current";
    public static final String CHANNEL_S1TOTALSOLARENERGY = "s1-total-solar-energy";
    public static final String CHANNEL_S1RELAYSTATUS = "s1-relay-status";
    public static final String CHANNEL_S1TEMPERATURE = "s1-temperature";
    public static final String CHANNEL_S1FAULTCODE = "s1-fault-code";
    public static final String CHANNEL_S1DCLINKVOLTAGE = "s1-dc-link-voltage";
    public static final String CHANNEL_S1TIMESTAMP = "s1-timestamp";
    public static final String CHANNEL_S2ID = "s2-id";
    public static final String CHANNEL_S2PVVOLTAGE = "s2-pv-voltage";
    public static final String CHANNEL_S2PVCURRENT = "s2-pv-current";
    public static final String CHANNEL_S2TOTALSOLARENERGY = "s2-total-solar-energy";
    public static final String CHANNEL_S2RELAYSTATUS = "s2-relay-status";
    public static final String CHANNEL_S2TEMPERATURE = "s2-temperature";
    public static final String CHANNEL_S2FAULTCODE = "s2-fault-code";
    public static final String CHANNEL_S2DCLINKVOLTAGE = "s2-dc-link-voltage";
    public static final String CHANNEL_S2TIMESTAMP = "s2-timestamp";
    public static final String CHANNEL_S3ID = "s3-id";
    public static final String CHANNEL_S3PVVOLTAGE = "s3-pv-voltage";
    public static final String CHANNEL_S3PVCURRENT = "s3-pv-current";
    public static final String CHANNEL_S3TOTALSOLARENERGY = "s3-total-solar-energy";
    public static final String CHANNEL_S3RELAYSTATUS = "s3-relay-status";
    public static final String CHANNEL_S3TEMPERATURE = "s3-temperature";
    public static final String CHANNEL_S3FAULTCODE = "s3-fault-code";
    public static final String CHANNEL_S3DCLINKVOLTAGE = "s3-dc-link-voltage";
    public static final String CHANNEL_S3TIMESTAMP = "s3-timestamp";
    public static final String CHANNEL_S4ID = "s4-id";
    public static final String CHANNEL_S4PVVOLTAGE = "s4-pv-voltage";
    public static final String CHANNEL_S4PVCURRENT = "s4-pv-current";
    public static final String CHANNEL_S4TOTALSOLARENERGY = "s4-total-solar-energy";
    public static final String CHANNEL_S4RELAYSTATUS = "s4-relay-status";
    public static final String CHANNEL_S4TEMPERATURE = "s4-temperature";
    public static final String CHANNEL_S4FAULTCODE = "s4-fault-code";
    public static final String CHANNEL_S4DCLINKVOLTAGE = "s4-dc-link-voltage";
    public static final String CHANNEL_S4TIMESTAMP = "s4-timestamp";

    // List of ESO Channel ids
    public static final String CHANNEL_ESOID = "eso-id";
    public static final String CHANNEL_ESOVOLTAGEBATTERY = "eso-voltage-battery";
    public static final String CHANNEL_ESOCURRENTBATTERY = "eso-current-battery";
    public static final String CHANNEL_ESOBATTERYENERGYPRODUCED = "eso-battery-energy-produced";
    public static final String CHANNEL_ESOBATTERYENERGYCONSUMED = "eso-battery-energy-consumed";
    public static final String CHANNEL_ESOSOC = "eso-soc";
    public static final String CHANNEL_ESORELAYSTATUS = "eso-relay-status";
    public static final String CHANNEL_ESOTEMPERATURE = "eso-temperature";
    public static final String CHANNEL_ESOFAULTCODE = "eso-fault-code";
    public static final String CHANNEL_ESODCLINKVOLTAGE = "eso-dc-link-voltage";
    public static final String CHANNEL_ESOTIMESTAMP = "eso-timestamp";

    // List of ESM Channel ids
    public static final String CHANNEL_ESMID = "esm-id";
    public static final String CHANNEL_ESMSOH = "esm-soh";
    public static final String CHANNEL_ESMSOC = "esm-soc";
    public static final String CHANNEL_ESMTOTALCAPACITY = "esm-total-capacity";
    public static final String CHANNEL_ESMPOWERBATTERY = "esm-power-battery";
    public static final String CHANNEL_ESMSTATUS = "esm-status";
    public static final String CHANNEL_ESMTIMESTAMP = "esm-timestamp";

    // List of all Channel ids for configuration
    public static final String CHANNEL_REQUESTCHARGE = "request-charge";
    public static final String CHANNEL_REQUESTDISCHARGE = "request-discharge";
    public static final String CHANNEL_AUTO = "request-auto";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGYHUB);
}
