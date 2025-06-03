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
    public static final String CHANNEL_GRIDFREQUENCY = "id";
    public static final String CHANNEL_ACECURRENTL1 = "id";
    public static final String CHANNEL_ACECURRENTL2 = "id";
    public static final String CHANNEL_ACECURRENTL3 = "id";
    public static final String CHANNEL_GRIDVOLTAGEL1 = "id";
    public static final String CHANNEL_GRIDVOLTAGEL2 = "id";
    public static final String CHANNEL_GRIDVOLTAGEL3 = "id";
    public static final String CHANNEL_INVERTERRMSCURRENTL1 = "id";
    public static final String CHANNEL_INVERTERRMSCURRENTL2 = "id";
    public static final String CHANNEL_INVERTERRMSCURRENTL3 = "id";
    public static final String CHANNEL_INVERTERREACTIVECURRENTL1 = "id";
    public static final String CHANNEL_INVERTERREACTIVECURRENTL2 = "id";
    public static final String CHANNEL_INVERTERREACTIVECURRENTL3 = "id";
    public static final String CHANNEL_INVERTERACTIVECURRENTL1 = "id";
    public static final String CHANNEL_INVERTERACTIVECURRENTL2 = "id";
    public static final String CHANNEL_INVERTERACTIVECURRENTL3 = "id";
    public static final String CHANNEL_GRIDCURRENTL1 = "id";
    public static final String CHANNEL_GRIDCURRENTL2 = "id";
    public static final String CHANNEL_GRIDCURRENTL3 = "id";
    public static final String CHANNEL_GRIDREACTIVECURRENTL1 = "id";
    public static final String CHANNEL_GRIDREACTIVECURRENTL2 = "id";
    public static final String CHANNEL_GRIDREACTIVECURRENTL3 = "id";
    public static final String CHANNEL_GRIDACTIVECURRENTL1 = "id";
    public static final String CHANNEL_GRIDACTIVECURRENTL2 = "id";
    public static final String CHANNEL_GRIDACTIVECURRENTL3 = "id";
    public static final String CHANNEL_INVERTERLOADREACTIVECURRENTL1 = "id";
    public static final String CHANNEL_INVERTERLOADREACTIVECURRENTL2 = "id";
    public static final String CHANNEL_INVERTERLOADREACTIVECURRENTL3 = "id";
    public static final String CHANNEL_INVERTERLOADACTIVECURRENTL1 = "id";
    public static final String CHANNEL_INVERTERLOADACTIVECURRENTL2 = "id";
    public static final String CHANNEL_INVERTERLOADACTIVECURRENTL3 = "id";
    public static final String CHANNEL_APPARENTPOWER = "id";
    public static final String CHANNEL_GRIDPOWERACTIVEL1 = "id";
    public static final String CHANNEL_GRIDPOWERACTIVEL2 = "id";
    public static final String CHANNEL_GRIDPOWERACTIVEL3 = "id";
    public static final String CHANNEL_GRIDPOWERREACTIVEL1 = "id";
    public static final String CHANNEL_GRIDPOWERREACTIVEL2 = "id";
    public static final String CHANNEL_GRIDPOWERREACTIVEL3 = "id";
    public static final String CHANNEL_INVERTERPOWERACTIVEL1 = "id";
    public static final String CHANNEL_INVERTERPOWERACTIVEL2 = "id";
    public static final String CHANNEL_INVERTERPOWERACTIVEL3 = "id";
    public static final String CHANNEL_INVERTERPOWERREACTIVEL1 = "id";
    public static final String CHANNEL_INVERTERPOWERREACTIVEL2 = "id";
    public static final String CHANNEL_INVERTERPOWERREACTIVEL3 = "id";
    public static final String CHANNEL_CONSUMPTIONPOWERL1 = "id";
    public static final String CHANNEL_CONSUMPTIONPOWERL2 = "id";
    public static final String CHANNEL_CONSUMPTIONPOWERL3 = "id";
    public static final String CHANNEL_CONSUMPTIONPOWERREACTIVEL1 = "id";
    public static final String CHANNEL_CONSUMPTIONPOWERREACTIVEL2 = "id";
    public static final String CHANNEL_CONSUMPTIONPOWERREACTIVEL3 = "id";
    public static final String CHANNEL_SOLARPV = "id";
    public static final String CHANNEL_POSITIVEDCLINKVOLTAGE = "id";
    public static final String CHANNEL_NEGATIVEDCLINKVOLTAGE = "id";
    public static final String CHANNEL_GRIDENERGYPRODUCEDL1 = "id";
    public static final String CHANNEL_GRIDENERGYPRODUCEDL2 = "id";
    public static final String CHANNEL_GRIDENERGYPRODUCEDL3 = "id";
    public static final String CHANNEL_GRIDENERGYCONSUMEDL1 = "id";
    public static final String CHANNEL_GRIDENERGYCONSUMEDL2 = "id";
    public static final String CHANNEL_GRIDENERGYCONSUMEDL3 = "id";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDL1 = "id";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDL2 = "id";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDL3 = "id";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDL1 = "id";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDL2 = "id";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDL3 = "id";
    public static final String CHANNEL_LOADENERGYPRODUCEDL1 = "id";
    public static final String CHANNEL_LOADENERGYPRODUCEDL2 = "id";
    public static final String CHANNEL_LOADENERGYPRODUCEDL3 = "id";
    public static final String CHANNEL_LOADENERGYCONSUMEDL1 = "id";
    public static final String CHANNEL_LOADENERGYCONSUMEDL2 = "id";
    public static final String CHANNEL_LOADENERGYCONSUMEDL3 = "id";
    public static final String CHANNEL_GRIDENERGYPRODUCEDTOTAL = "id";
    public static final String CHANNEL_GRIDENERGYCONSUMEDTOTAL = "id";
    public static final String CHANNEL_INVERTERENERGYPRODUCEDTOTAL = "id";
    public static final String CHANNEL_INVERTERENERGYCONSUMEDTOTAL = "id";
    public static final String CHANNEL_LOADENERGYPRODUCEDTOTAL = "id";
    public static final String CHANNEL_LOADENERGYCONSUMEDTOTAL = "id";
    public static final String CHANNEL_TOTALSOLARENERGY = "id";
    public static final String CHANNEL_STATE = "id";
    public static final String CHANNEL_TIMESTAMP = "id";

    // List of battery setup Channel ids
    public static final String CHANNEL_BATTERYENERGYPRODUCED = "id";
    public static final String CHANNEL_BATTERYENERGYCONSUMED = "id";
    public static final String CHANNEL_SOC = "id";
    public static final String CHANNEL_SOH = "id";
    public static final String CHANNEL_POWERBATTERY = "id";
    public static final String CHANNEL_TOTALCAPACITYBATTERIES = "id";

    // List of SSO Channel ids
    public static final String CHANNEL_S1ID = "id";
    public static final String CHANNEL_S1PVVOLTAGE = "id";
    public static final String CHANNEL_S1PVCURRENT = "id";
    public static final String CHANNEL_S1TOTALSOLARENERGY = "id";
    public static final String CHANNEL_S1RELAYSTATUS = "id";
    public static final String CHANNEL_S1TEMPERATURE = "id";
    public static final String CHANNEL_S1FAULTCODE = "id";
    public static final String CHANNEL_S1DCLINKVOLTAGE = "id";
    public static final String CHANNEL_S1TIMESTAMP = "id";
    public static final String CHANNEL_S2ID = "id";
    public static final String CHANNEL_S2PVVOLTAGE = "id";
    public static final String CHANNEL_S2PVCURRENT = "id";
    public static final String CHANNEL_S2TOTALSOLARENERGY = "id";
    public static final String CHANNEL_S2RELAYSTATUS = "id";
    public static final String CHANNEL_S2TEMPERATURE = "id";
    public static final String CHANNEL_S2FAULTCODE = "id";
    public static final String CHANNEL_S2DCLINKVOLTAGE = "id";
    public static final String CHANNEL_S2TIMESTAMP = "id";
    public static final String CHANNEL_S3ID = "id";
    public static final String CHANNEL_S3PVVOLTAGE = "id";
    public static final String CHANNEL_S3PVCURRENT = "id";
    public static final String CHANNEL_S3TOTALSOLARENERGY = "id";
    public static final String CHANNEL_S3RELAYSTATUS = "id";
    public static final String CHANNEL_S3TEMPERATURE = "id";
    public static final String CHANNEL_S3FAULTCODE = "id";
    public static final String CHANNEL_S3DCLINKVOLTAGE = "id";
    public static final String CHANNEL_S3TIMESTAMP = "id";
    public static final String CHANNEL_S4ID = "id";
    public static final String CHANNEL_S4PVVOLTAGE = "id";
    public static final String CHANNEL_S4PVCURRENT = "id";
    public static final String CHANNEL_S4TOTALSOLARENERGY = "id";
    public static final String CHANNEL_S4RELAYSTATUS = "id";
    public static final String CHANNEL_S4TEMPERATURE = "id";
    public static final String CHANNEL_S4FAULTCODE = "id";
    public static final String CHANNEL_S4DCLINKVOLTAGE = "id";
    public static final String CHANNEL_S4TIMESTAMP = "id";

    // List of ESO Channel ids
    public static final String CHANNEL_ESOID = "id";
    public static final String CHANNEL_ESOVOLTAGEBATTERY = "id";
    public static final String CHANNEL_ESOCURRENTBATTERY = "id";
    public static final String CHANNEL_ESOBATTERYENERGYPRODUCED = "id";
    public static final String CHANNEL_ESOBATTERYENERGYCONSUMED = "id";
    public static final String CHANNEL_ESOSOC = "id";
    public static final String CHANNEL_ESORELAYSTATUS = "id";
    public static final String CHANNEL_ESOTEMPERATURE = "id";
    public static final String CHANNEL_ESOFAULTCODE = "id";
    public static final String CHANNEL_ESODCLINKVOLTAGE = "id";
    public static final String CHANNEL_ESOTIMESTAMP = "id";

    // List of ESM Channel ids
    public static final String CHANNEL_ESMID = "id";
    public static final String CHANNEL_ESMSOH = "id";
    public static final String CHANNEL_ESMSOC = "id";
    public static final String CHANNEL_ESMTOTALCAPACITY = "id";
    public static final String CHANNEL_ESMPOWERBATTERY = "id";
    public static final String CHANNEL_ESMSTATUS = "id";
    public static final String CHANNEL_ESMTIMESTAMP = "id";

    // List of all Channel ids for configuration
    public static final String CHANNEL_REQUESTCHARGE = "id";
    public static final String CHANNEL_REQUESTDISCHARGE = "id";
    public static final String CHANNEL_AUTO = "id";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGYHUB);
}
