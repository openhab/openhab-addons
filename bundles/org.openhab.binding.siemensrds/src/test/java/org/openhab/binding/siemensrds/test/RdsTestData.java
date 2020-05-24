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
package org.openhab.binding.siemensrds.test;

import static org.junit.Assert.*;
import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.openhab.binding.siemensrds.internal.RdsAccessToken;
import org.openhab.binding.siemensrds.internal.RdsCloudException;
import org.openhab.binding.siemensrds.internal.RdsDataPoints;
import org.openhab.binding.siemensrds.internal.RdsPlants;
import org.openhab.binding.siemensrds.internal.RdsPlants.PlantInfo;
import org.openhab.binding.siemensrds.points.BasePoint;

import tec.uom.se.unit.Units;

/**
 * test suite
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class RdsTestData {

    /*
     * note: temperature symbols with a degree sign: the MVN Spotless formatter
     * trashes the "degree" (looks like *) symbol, so we must escape these symbols
     * as octal \260 or unicode \u00B00 in the following JSON test strings
     * 
     * note: (at)formatter on/off tags instruct spotless not to reformat the JSON,
     * (and perhaps also attempt to stop it trashing the degree symbols - see above)
     */
    //@formatter:off
    private static final String DATAPOINTS_JSON_FULL_SET = 
        "{" + 
        "    \"totalCount\": 67," + 
        "    \"values\": {" + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF00000C\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"AAS-20:SU=SiUn;APT=HvacFnct18z_A;APTV=2.003;APS=1;\"," + 
        "            \"descriptionName\": \"ApplicationSoftwareVersion\"," + 
        "            \"objectName\": \"ApplicationSoftwareVersion\"," + 
        "            \"memberName\": \"ApplicationSoftwareVersion\"," + 
        "            \"hierarchyName\": \"ApplicationSoftwareVersion\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF00001C\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"Device object\"," + 
        "            \"descriptionName\": \"Device Description\"," + 
        "            \"objectName\": \"Device Description\"," + 
        "            \"memberName\": \"Description\"," + 
        "            \"hierarchyName\": \"Device Description\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF00002C\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"FW=02.32.02.27;SVS-300.1:SBC=13.22;I\"," + 
        "            \"descriptionName\": \"FirmwareRevision\"," + 
        "            \"objectName\": \"FirmwareRevision\"," + 
        "            \"memberName\": \"FirmwareRevision\"," + 
        "            \"hierarchyName\": \"FirmwareRevision\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF000046\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"RDS110\"," + 
        "            \"descriptionName\": \"ModelName\"," + 
        "            \"objectName\": \"ModelName\"," + 
        "            \"memberName\": \"ModelName\"," + 
        "            \"hierarchyName\": \"ModelName\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF000070\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 0," + 
        "            \"limits\": [0.0, 5.0]," + 
        "            \"descr\": \"operational*operational-read-only*download-required*download-in-progress*non-operational*backup-in-progress\"," + 
        "            \"descriptionName\": \"SystemStatus\"," + 
        "            \"objectName\": \"SystemStatus\"," + 
        "            \"memberName\": \"SystemStatus\"," + 
        "            \"hierarchyName\": \"SystemStatus\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF000077\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 0," + 
        "            \"descriptionName\": \"UtcOffset\"," + 
        "            \"objectName\": \"UtcOffset\"," + 
        "            \"memberName\": \"UtcOffset\"," + 
        "            \"hierarchyName\": \"UtcOffset\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF00009B\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 19," + 
        "            \"descriptionName\": \"DatabaseRevision\"," + 
        "            \"objectName\": \"DatabaseRevision\"," + 
        "            \"memberName\": \"DatabaseRevision\"," + 
        "            \"hierarchyName\": \"DatabaseRevision\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF0000C4\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 0," + 
        "            \"limits\": [0.0, 7.0]," + 
        "            \"descr\": \"unknown*coldstart*warmstart*detected-power-lost*detected-powered-off*hardware-watchdog*software-watchdog*suspended\"," + 
        "            \"descriptionName\": \"LastRestartReason\"," + 
        "            \"objectName\": \"LastRestartReason\"," + 
        "            \"memberName\": \"LastRestartReason\"," + 
        "            \"hierarchyName\": \"LastRestartReason\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF0012DB\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"MDL:ASN= RDS110;HW=0.2.0;\"," + 
        "            \"descriptionName\": \"ModelInformation\"," + 
        "            \"objectName\": \"ModelInformation\"," + 
        "            \"memberName\": \"4827\"," + 
        "            \"hierarchyName\": \"ModelInformation\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF001355\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 1," + 
        "            \"limits\": [0.0, 26.0]," + 
        "            \"descr\": \"-*en*de*fr*es*cs*da*nl*fi*it*hu*nb*pl*pt*ru*sk*sv*zh*zh*ko*ro*tr*en-US*fr-CA*es-mx*pt-BR\"," + 
        "            \"descriptionName\": \"Active SystemLanguge\"," + 
        "            \"objectName\": \"Active SystemLanguge\"," + 
        "            \"memberName\": \"4949\"," + 
        "            \"hierarchyName\": \"Active SystemLanguge\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF0013B0\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 26," + 
        "            \"limits\": [0.0, 72.0]," + 
        "            \"descr\": \"GMT-12:00 Kwajalein*GMT-11:00 Samoa, Midway*GMT-10:00 Hawaii*GMT-09:00 Alaska*GMT-08:00 Pacific Time*GMT-07:00 Arizona*GMT-07:00 Chihuahua*GMT-07:00 Mountain Time*GMT-06:00 Central America*GMT-06:00 Central Time*GMT-06:00 Mexico City*GMT-06:00 Saskatchewan*GMT-05:00 Bogota, Lima*GMT-05:00 Eastern Time*GMT-05:00 Indiana (USA)*GMT-04:00 Atlantic Time*GMT-04:00 Caracas, La Paz*GMT-04:00 Santiago*18*GMT-03:00 Brasilia*20*21*GMT-02:00 Mid-Atlantic*23*24*25*GMT London, Dublin, Lisbon*GMT+01:00 Berlin, Rome*GMT+01:00 Budapest, Prague*GMT+01:00 Paris, Madrid*GMT+01:00 Vienna, Warsaw*31*GMT+02:00 Athens, Istanbul*GMT+02:00 Bucharest*GMT+02:00 Cairo*GMT+02:00 Johannesburg, Harare*GMT+02:00 Helsinki, Riga*GMT+02:00 Jerusalem*38*GMT+03:00 Kuwait, Riyadh*GMT+04:00 Moscow*41*GMT+03:30 Tehran*GMT+04:00 Abu Dhabi, Muscat*GMT+04:00 Baku, Tbilisi*45*GMT+06:00 Ekaterinburg*47*GMT+05:30 New Delhi*49*GMT+07:00 Omsk, Novosibirsk *51*52*53*GMT+07:00 Bangkok, Jakarta*GMT+08:00 Krasnoyarsk*GMT+08:00 Beijing, Hong Kong*GMT+09:00 Irkutsk*GMT+08:00 Kuala Lumpur*GMT+08:00 Perth*GMT+08:00 Taipei*GMT+09:00 Tokyo, Osaka*GMT+09:00 Seoul*GMT+10:00 Yakutsk*GMT+09:30 Adelaide*GMT+09:30 Darwin*GMT+10:00 Brisbane*GMT+10:00 Melbourne, Sydney*68*69*GMT+11:00 Vladivostok*71*GMT+12:00 Auckland, Wellington\"," + 
        "            \"descriptionName\": \"TimeZone\"," + 
        "            \"objectName\": \"TimeZone\"," + 
        "            \"memberName\": \"5040\"," + 
        "            \"hierarchyName\": \"TimeZone\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF0013EC\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"160100096D\"," + 
        "            \"descriptionName\": \"SerialNumber\"," + 
        "            \"objectName\": \"SerialNumber\"," + 
        "            \"memberName\": \"5100\"," + 
        "            \"hierarchyName\": \"SerialNumber\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!0083FFFFF0013F4\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"'10010'B\"," + 
        "            \"descriptionName\": \"Device Features\"," + 
        "            \"objectName\": \"Device Features\"," + 
        "            \"memberName\": \"5108\"," + 
        "            \"hierarchyName\": \"Device Features\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!01D00000700001C\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": \"Upstairs\"," + 
        "            \"descriptionName\": \"R(1)'Description\"," + 
        "            \"objectName\": \"R(1)'Description\"," + 
        "            \"memberName\": \"Description\"," + 
        "            \"hierarchyName\": \"R(1)'Description\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!10800000000130B\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"192.168.1.1\"," + 
        "            \"descriptionName\": \"NwkPortIP'IP gefault gateway\"," + 
        "            \"objectName\": \"NwkPortIP'IP gefault gateway\"," + 
        "            \"memberName\": \"4875\"," + 
        "            \"hierarchyName\": \"NwkPortIP'IP gefault gateway\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!10800000000130C\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"255.255.255.0\"," + 
        "            \"descriptionName\": \"NwkPortIP'IP subnet mask\"," + 
        "            \"objectName\": \"NwkPortIP'IP subnet mask\"," + 
        "            \"memberName\": \"4876\"," + 
        "            \"hierarchyName\": \"NwkPortIP'IP subnet mask\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!10800000000130D\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"192.168.1.42\"," + 
        "            \"descriptionName\": \"NwkPortIP'IP address\"," + 
        "            \"objectName\": \"NwkPortIP'IP address\"," + 
        "            \"memberName\": \"4877\"," + 
        "            \"hierarchyName\": \"NwkPortIP'IP address\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!10800000000130E\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 47808," + 
        "            \"descriptionName\": \"NwkPortIP'UDP Port\"," + 
        "            \"objectName\": \"NwkPortIP'UDP Port\"," + 
        "            \"memberName\": \"4878\"," + 
        "            \"hierarchyName\": \"NwkPortIP'UDP Port\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!108000000001313\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"'F0C77F6C1895'H\"," + 
        "            \"descriptionName\": \"NwkPortIP'BACnet MAC address\"," + 
        "            \"objectName\": \"NwkPortIP'BACnet MAC address\"," + 
        "            \"memberName\": \"4883\"," + 
        "            \"hierarchyName\": \"NwkPortIP'BACnet MAC address\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!108000001001286\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"sth.connectivity.ccl-siemens.com\"," + 
        "            \"descriptionName\": \"NwkPortCCL'Connection URI\"," + 
        "            \"objectName\": \"NwkPortCCL'Connection URI\"," + 
        "            \"memberName\": \"4742\"," + 
        "            \"hierarchyName\": \"NwkPortCCL'Connection URI\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!108000001001287\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"this-is-not-a-valid-activation-key\"," + 
        "            \"descriptionName\": \"NwkPortCCL'Activation Key\"," + 
        "            \"objectName\": \"NwkPortCCL'Activation Key\"," + 
        "            \"memberName\": \"4743\"," + 
        "            \"hierarchyName\": \"NwkPortCCL'Activation Key\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!108000001001288\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 60," + 
        "            \"descriptionName\": \"NwkPortCCL'Reconection delay\"," + 
        "            \"objectName\": \"NwkPortCCL'Reconection delay\"," + 
        "            \"memberName\": \"4744\"," + 
        "            \"hierarchyName\": \"NwkPortCCL'Reconection delay\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!DPUpdPerMin\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 0," + 
        "            \"descriptionName\": \"Target\"," + 
        "            \"objectName\": \"Target\"," + 
        "            \"memberName\": \"Item Updates per Minute\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!DPUpdTotal\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 286849," + 
        "            \"descriptionName\": \"Target\"," + 
        "            \"objectName\": \"Target\"," + 
        "            \"memberName\": \"Item Updates Total\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!Languages\": {" + 
        "            \"rep\": 0," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": \"-;en\"," + 
        "            \"limits\": [0.0, 1.0]," + 
        "            \"objectName\": \"CSL-Config\"," + 
        "            \"memberName\": \"Languages\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!Online\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 1," + 
        "            \"limits\": [0.0, 1.0]," + 
        "            \"descr\": \"Offline*Online\"," + 
        "            \"descriptionName\": \"Target\"," + 
        "            \"objectName\": \"Target\"," + 
        "            \"memberName\": \"Online\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!TrfcInPerMin\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 1473," + 
        "            \"descr\": \"bytes\"," + 
        "            \"descriptionName\": \"Target\"," + 
        "            \"objectName\": \"Target\"," + 
        "            \"memberName\": \"Traffic Inbound per Minute\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!TrfcInTotal\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 178130801," + 
        "            \"descr\": \"bytes\"," + 
        "            \"descriptionName\": \"Target\"," + 
        "            \"objectName\": \"Target\"," + 
        "            \"memberName\": \"Traffic Inbound Total\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!TrfcOutPerMin\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 616," + 
        "            \"descr\": \"bytes\"," + 
        "            \"descriptionName\": \"Target\"," + 
        "            \"objectName\": \"Target\"," + 
        "            \"memberName\": \"Traffic Outbound per Minute\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!TrfcOutTotal\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": 60624666," + 
        "            \"descr\": \"bytes\"," + 
        "            \"descriptionName\": \"Target\"," + 
        "            \"objectName\": \"Target\"," + 
        "            \"memberName\": \"Traffic Outbound Total\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!00000000E000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": {" + 
        "                \"value\": 18.5519028," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": -50.0," + 
        "                \"maxValue\": 80.0" + 
        "            }," + 
        "            \"limits\": [-50.0, 80.0]," + 
        "            \"descr\": \"\260C\"," + 
        "            \"descriptionName\": \"Outside air temperature\"," + 
        "            \"objectName\": \"R(1)'TOa\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'TOa\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000002000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 0.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 5.0" + 
        "            }," + 
        "            \"limits\": [0.0, 5.0]," + 
        "            \"descr\": \"A\"," + 
        "            \"descriptionName\": \"Heating device electrical load\"," + 
        "            \"objectName\": \"R(1)'HDevElLd\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'HDevElLd\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!00200007F000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 24.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 17," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 50.0" + 
        "            }," + 
        "            \"limits\": [0.0, 50.0]," + 
        "            \"descr\": \"\260C\"," + 
        "            \"descriptionName\": \"Comfort heating setpoint\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHCmf\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'SpHCmf\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000080000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 24.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 50.0" + 
        "            }," + 
        "            \"limits\": [0.0, 50.0]," + 
        "            \"descr\": \"\260F\"," + 
        "            \"descriptionName\": \"Pre-comfort heating setpoint\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHPcf\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'SpHPcf\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000081000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 16.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 50.0" + 
        "            }," + 
        "            \"limits\": [0.0, 50.0]," + 
        "            \"descr\": \"\260C\"," + 
        "            \"descriptionName\": \"Economy heating setpoint\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHEco\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'SpHEco\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000082000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 6.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 50.0" + 
        "            }," + 
        "            \"limits\": [0.0, 50.0]," + 
        "            \"descr\": \"\260C\"," + 
        "            \"descriptionName\": \"Protection heating setpoint\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHPrt\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'SpHPrt\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000083000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 24.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 15," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 6.0," + 
        "                \"maxValue\": 35.0" + 
        "            }," + 
        "            \"limits\": [12.0, 35.0]," + 
        "            \"descr\": \"\260C\"," + 
        "            \"descriptionName\": \"Room temperature setpoint\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'SpTRDtr'SpTR\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'SpTR\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000084000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 0.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 15," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": -18.0," + 
        "                \"maxValue\": 11.0" + 
        "            }," + 
        "            \"limits\": [-9.0, 14.0]," + 
        "            \"descr\": \"K\"," + 
        "            \"descriptionName\": \"Room temperature setpoint shift\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'SpTRDtr'SpTRShft\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'SpTRShft\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.10000000149011612" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000085000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": {" + 
        "                \"value\": 46.86865," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 100.0" + 
        "            }," + 
        "            \"limits\": [0.0, 100.0]," + 
        "            \"descr\": \"%r.H.\"," + 
        "            \"descriptionName\": \"Relative humidity for room\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'RHuRel\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'RHuRel\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 1.0" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000086000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": {" + 
        "                \"value\": 23.761879," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 50.0" + 
        "            }," + 
        "            \"limits\": [0.0, 50.0]," + 
        "            \"descr\": \"\260C\"," + 
        "            \"descriptionName\": \"Room temperature\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'RTemp\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'RTemp\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.10000000149011612" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!0020000B2000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 35.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 50.0" + 
        "            }," + 
        "            \"limits\": [0.0, 50.0]," + 
        "            \"descr\": \"\260C\"," + 
        "            \"descriptionName\": \"Max. heating setpoint\"," + 
        "            \"objectName\": \"R(1)'SpTRMaxHCmf\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'SpTRMaxHCmf\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!0020000B4000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 30.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 120.0" + 
        "            }," + 
        "            \"limits\": [0.0, 120.0]," + 
        "            \"descr\": \"Unknown Unit(236)\"," + 
        "            \"descriptionName\": \"Warm-up gradient\"," + 
        "            \"objectName\": \"R(1)'WarmUpGrdnt\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'WarmUpGrdnt\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 5.0" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!0020000B5000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 1.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": -5.0," + 
        "                \"maxValue\": 5.0" + 
        "            }," + 
        "            \"limits\": [-5.0, 5.0]," + 
        "            \"descr\": \"K\"," + 
        "            \"descriptionName\": \"Built-in temp. sensor adj.\"," + 
        "            \"objectName\": \"R(1)'TRBltnMsvAdj\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'TRBltnMsvAdj\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!0020000CB000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 0.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 5.0" + 
        "            }," + 
        "            \"limits\": [0.0, 5.0]," + 
        "            \"descr\": \"A\"," + 
        "            \"descriptionName\": \"Q22/Q24 electrical load\"," + 
        "            \"objectName\": \"R(1)'Q22Q24ElLd\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'Q22Q24ElLd\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 0.5" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!0020000CD000055\": {" + 
        "            \"rep\": 1," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": {" + 
        "                \"value\": 713.0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 5000.0" + 
        "            }," + 
        "            \"limits\": [0.0, 5000.0]," + 
        "            \"descr\": \"ppm\"," + 
        "            \"descriptionName\": \"Room air quality\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'RAQual\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'RAQual\"," + 
        "            \"translated\": false," + 
        "            \"resolution\": 100.0" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!005000038000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 13," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"Inactive*Active*Null\"," + 
        "            \"descriptionName\": \"Temporary comfort button\"," + 
        "            \"objectName\": \"R(1)'ROpModDtr'TmpCmfBtn\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'TmpCmfBtn\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!005000039000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 13," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"Inactive*Active*Null\"," + 
        "            \"descriptionName\": \"Comfort button\"," + 
        "            \"objectName\": \"R(1)'ROpModDtr'CmfBtn\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'CmfBtn\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!00500003B000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": {" + 
        "                \"value\": 0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"Absent*Present*Null\"," + 
        "            \"descriptionName\": \"Room presence detection\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'RPscDet\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'RPscDet\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!00500003F000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 1," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 17," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"No*Yes*Null\"," + 
        "            \"descriptionName\": \"Enable heating control\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'TCtlH'EnHCtl\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'EnHCtl\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!005000054000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 0," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"Inactive*Active*Null\"," + 
        "            \"descriptionName\": \"Room presence detector\"," + 
        "            \"objectName\": \"R(1)'EnRPscDet\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'EnRPscDet\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!01300004C000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 2," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"Null*Off*Protection\"," + 
        "            \"descriptionName\": \"Off/protection configuration\"," + 
        "            \"objectName\": \"R(1)'OffPrtCnf\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'OffPrtCnf\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000051000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 3," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 13," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 3.0]," + 
        "            \"descr\": \"Null*Off*Absent*Present\"," + 
        "            \"descriptionName\": \"Occupancy mode\"," + 
        "            \"objectName\": \"R(1)'ROpModDtr'OccMod\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'OccMod\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000052000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 5," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 15," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 5.0]," + 
        "            \"descr\": \"Null*Undefined*Poor*Satisfactory*Good*Excellent\"," + 
        "            \"descriptionName\": \"Energy efficiency indication room\"," + 
        "            \"objectName\": \"R(1)'RGrnLf'REei\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'REei\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000053000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 2," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 15," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"Null*Off*On\"," + 
        "            \"descriptionName\": \"Domestic hot water mode\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'DhwOp'DhwMod\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'DhwMod\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000056000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": {" + 
        "                \"value\": 2," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 3.0]," + 
        "            \"descr\": \"Null*Neither*Heating*Cooling\"," + 
        "            \"descriptionName\": \"Heating/cooling state\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'HCStaDtr'HCSta\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'HCSta\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!01300005A000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 4," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"presentPriority\": 15," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 4.0]," + 
        "            \"descr\": \"Null*Protection*Economy*Pre-Comfort*Comfort\"," + 
        "            \"descriptionName\": \"Present operating mode and reason\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'PrOpModRsn\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'PrOpModRsn\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000071000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 6," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 6.0]," + 
        "            \"descr\": \"Null*Default*Slow*Medium*Fast*2-position*Self-adaptive\"," + 
        "            \"descriptionName\": \"Heating control loop\"," + 
        "            \"objectName\": \"R(1)'HCtrSet\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'HCtrSet\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000072000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 2," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descr\": \"Null*Warm-up gradient*Self-adaptive\"," + 
        "            \"descriptionName\": \"Optimum start control setting\"," + 
        "            \"objectName\": \"R(1)'OsscSet\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'OsscSet\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000074000055\": {" + 
        "            \"rep\": 3," + 
        "            \"type\": 0," + 
        "            \"write\": false," + 
        "            \"value\": {" + 
        "                \"value\": 4," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 4.0]," + 
        "            \"descr\": \"Null*Undefined*Poor*Okay*Good\"," + 
        "            \"descriptionName\": \"Room air quality indication\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'RAQualInd\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'RAQualInd\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!030000000000055\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 500," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 1.0," + 
        "                \"maxValue\": 8760.0" + 
        "            }," + 
        "            \"limits\": [1.0, 8760.0]," + 
        "            \"descr\": \"h\"," + 
        "            \"descriptionName\": \"Pump/valve kick cycle\"," + 
        "            \"objectName\": \"R(1)'KickCyc\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'KickCyc\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!030000001000055\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 180000," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 3600000.0" + 
        "            }," + 
        "            \"limits\": [0.0, 3600000.0]," + 
        "            \"descr\": \"ms\"," + 
        "            \"descriptionName\": \"DHW min. ON time\"," + 
        "            \"objectName\": \"R(1)'BoDhwTiOnMin\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'BoDhwTiOnMin\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!030000002000055\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 180000," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 3600000.0" + 
        "            }," + 
        "            \"limits\": [0.0, 3600000.0]," + 
        "            \"descr\": \"ms\"," + 
        "            \"descriptionName\": \"DHW min. OFF time\"," + 
        "            \"objectName\": \"R(1)'BoDhwTiOffMin\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrAplSet'BoDhwTiOffMin\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!030000007000055\": {" + 
        "            \"rep\": 2," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": 253140," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0," + 
        "                \"eventState\": 0," + 
        "                \"minValue\": 0.0," + 
        "                \"maxValue\": 4294967295.0" + 
        "            }," + 
        "            \"limits\": [0.0, 4294967295.0]," + 
        "            \"descr\": \"min\"," + 
        "            \"descriptionName\": \"Operating hours heating\"," + 
        "            \"objectName\": \"R(1)'RHvacCoo'OphHCDtr'OphH\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'OphH\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;2!011000004000055\": {" + 
        "            \"rep\": 8," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": {" + 
        "                    \"cmd\": \"Command\"," + 
        "                    \"cal\": null," + 
        "                    \"act\": 0," + 
        "                    \"null\": 3," + 
        "                    \"default\": 0," + 
        "                    \"schedStart\": 4294967295," + 
        "                    \"schedEnd\": 4294967295," + 
        "                    \"days\": [" + 
        "                        [" + 
        "                            [0, 3]," + 
        "                            [7687, 4]," + 
        "                            [7701, 3]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 3]," + 
        "                            [7687, 4]," + 
        "                            [7701, 3]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 3]," + 
        "                            [7687, 4]," + 
        "                            [7701, 3]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 3]," + 
        "                            [7687, 4]," + 
        "                            [7701, 3]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 3]," + 
        "                            [7687, 4]," + 
        "                            [7701, 3]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 3]," + 
        "                            [7687, 4]," + 
        "                            [7701, 3]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 3]," + 
        "                            [7687, 4]," + 
        "                            [7701, 3]" + 
        "                        ]," + 
        "                        []" + 
        "                    ]," + 
        "                    \"timeTillNextValue\": 632," + 
        "                    \"nextValue\": 3" + 
        "                }," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 4.0]," + 
        "            \"descriptionName\": \"Room operating mode scheduler\"," + 
        "            \"objectName\": \"R(1)'CenOpMod'ROpModSched\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'ROpModSched\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;2!011000005000055\": {" + 
        "            \"rep\": 8," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": {" + 
        "                    \"cmd\": \"Command\"," + 
        "                    \"cal\": null," + 
        "                    \"act\": 0," + 
        "                    \"null\": 3," + 
        "                    \"default\": 0," + 
        "                    \"schedStart\": 4294967295," + 
        "                    \"schedEnd\": 4294967295," + 
        "                    \"days\": [" + 
        "                        [" + 
        "                            [0, 1]," + 
        "                            [8, 2]," + 
        "                            [11543, 1]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 1]," + 
        "                            [8, 2]," + 
        "                            [11543, 1]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 1]," + 
        "                            [8, 2]," + 
        "                            [11543, 1]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 1]," + 
        "                            [8, 2]," + 
        "                            [11543, 1]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 1]," + 
        "                            [8, 2]," + 
        "                            [11543, 1]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 1]," + 
        "                            [8, 2]," + 
        "                            [11543, 1]" + 
        "                        ]," + 
        "                        [" + 
        "                            [0, 1]," + 
        "                            [8, 2]," + 
        "                            [11543, 1]" + 
        "                        ]," + 
        "                        []" + 
        "                    ]," + 
        "                    \"timeTillNextValue\": 767," + 
        "                    \"nextValue\": 1" + 
        "                }," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0" + 
        "            }," + 
        "            \"limits\": [0.0, 2.0]," + 
        "            \"descriptionName\": \"Domestic hot water scheduler\"," + 
        "            \"objectName\": \"R(1)'CenDhw'DhwSched\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'DhwSched\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;3!01100000400004E\": {" + 
        "            \"rep\": 9," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": {" + 
        "                    \"act\": 0," + 
        "                    \"rules\": [" + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]" + 
        "                    ]" + 
        "                }," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0" + 
        "            }," + 
        "            \"descriptionName\": \"Room operating mode scheduler\"," + 
        "            \"objectName\": \"R(1)'CenOpMod'ROpModSched\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'ROpModSched\"," + 
        "            \"translated\": false" + 
        "        }," + 
        "        \"Pd1774247-7de7-4896-ac76-b7e0dd943c40;3!01100000500004E\": {" + 
        "            \"rep\": 9," + 
        "            \"type\": 0," + 
        "            \"write\": true," + 
        "            \"value\": {" + 
        "                \"value\": {" + 
        "                    \"act\": 0," + 
        "                    \"rules\": [" + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]," + 
        "                        [3, 4294967295, 4294967295, 4294967295]" + 
        "                    ]" + 
        "                }," + 
        "                \"statusFlags\": 0," + 
        "                \"reliability\": 0" + 
        "            }," + 
        "            \"descriptionName\": \"Domestic hot water scheduler\"," + 
        "            \"objectName\": \"R(1)'CenDhw'DhwSched\"," + 
        "            \"memberName\": \"PresentValue\"," + 
        "            \"hierarchyName\": \"R(1)'FvrBscOp'DhwSched\"," + 
        "            \"translated\": false" + 
        "        }" + 
        "    }" + 
        "}";
    //@formatter:on

    //@formatter:off
    private static final String DATAPOINTS_JSON_FULL_SET_NEW = 
            "{\r\n" + 
            "  \"totalCount\": 70,\r\n" + 
            "  \"values\": {\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF00000C\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"AAS-20:SU=SiUn;APT=HvacFnct18z_A;APTV=2.003;APS=1;\",\r\n" + 
            "      \"descriptionName\": \"ApplicationSoftwareVersion\",\r\n" + 
            "      \"objectName\": \"ApplicationSoftwareVersion\",\r\n" + 
            "      \"memberName\": \"ApplicationSoftwareVersion\",\r\n" + 
            "      \"hierarchyName\": \"ApplicationSoftwareVersion\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF00001C\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"Device object\",\r\n" + 
            "      \"descriptionName\": \"Device Description\",\r\n" + 
            "      \"objectName\": \"Device Description\",\r\n" + 
            "      \"memberName\": \"Description\",\r\n" + 
            "      \"hierarchyName\": \"Device Description\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF00002C\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"FW=02.32.02.27;SVS-300.1:SBC=13.22;I\",\r\n" + 
            "      \"descriptionName\": \"FirmwareRevision\",\r\n" + 
            "      \"objectName\": \"FirmwareRevision\",\r\n" + 
            "      \"memberName\": \"FirmwareRevision\",\r\n" + 
            "      \"hierarchyName\": \"FirmwareRevision\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF000046\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"RDS110\",\r\n" + 
            "      \"descriptionName\": \"ModelName\",\r\n" + 
            "      \"objectName\": \"ModelName\",\r\n" + 
            "      \"memberName\": \"ModelName\",\r\n" + 
            "      \"hierarchyName\": \"ModelName\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF000070\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 0,\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        5\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"operational*operational-read-only*download-required*download-in-progress*non-operational*backup-in-progress\",\r\n" + 
            "      \"descriptionName\": \"SystemStatus\",\r\n" + 
            "      \"objectName\": \"SystemStatus\",\r\n" + 
            "      \"memberName\": \"SystemStatus\",\r\n" + 
            "      \"hierarchyName\": \"SystemStatus\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF000077\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 0,\r\n" + 
            "      \"descriptionName\": \"UtcOffset\",\r\n" + 
            "      \"objectName\": \"UtcOffset\",\r\n" + 
            "      \"memberName\": \"UtcOffset\",\r\n" + 
            "      \"hierarchyName\": \"UtcOffset\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF00009B\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 29,\r\n" + 
            "      \"descriptionName\": \"DatabaseRevision\",\r\n" + 
            "      \"objectName\": \"DatabaseRevision\",\r\n" + 
            "      \"memberName\": \"DatabaseRevision\",\r\n" + 
            "      \"hierarchyName\": \"DatabaseRevision\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF0000C4\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 4,\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        7\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"unknown*coldstart*warmstart*detected-power-lost*detected-powered-off*hardware-watchdog*software-watchdog*suspended\",\r\n" + 
            "      \"descriptionName\": \"LastRestartReason\",\r\n" + 
            "      \"objectName\": \"LastRestartReason\",\r\n" + 
            "      \"memberName\": \"LastRestartReason\",\r\n" + 
            "      \"hierarchyName\": \"LastRestartReason\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF0012DB\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"MDL:ASN= RDS110;HW=0.2.0;\",\r\n" + 
            "      \"descriptionName\": \"ModelInformation\",\r\n" + 
            "      \"objectName\": \"ModelInformation\",\r\n" + 
            "      \"memberName\": \"4827\",\r\n" + 
            "      \"hierarchyName\": \"ModelInformation\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF001355\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 1,\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        26\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"-*en*de*fr*es*cs*da*nl*fi*it*hu*nb*pl*pt*ru*sk*sv*zh*zh*ko*ro*tr*en-US*fr-CA*es-mx*pt-BR\",\r\n" + 
            "      \"descriptionName\": \"Active SystemLanguge\",\r\n" + 
            "      \"objectName\": \"Active SystemLanguge\",\r\n" + 
            "      \"memberName\": \"4949\",\r\n" + 
            "      \"hierarchyName\": \"Active SystemLanguge\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF0013B0\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 26,\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        72\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"GMT-12:00 Kwajalein*GMT-11:00 Samoa, Midway*GMT-10:00 Hawaii*GMT-09:00 Alaska*GMT-08:00 Pacific Time*GMT-07:00 Arizona*GMT-07:00 Chihuahua*GMT-07:00 Mountain Time*GMT-06:00 Central America*GMT-06:00 Central Time*GMT-06:00 Mexico City*GMT-06:00 Saskatchewan*GMT-05:00 Bogota, Lima*GMT-05:00 Eastern Time*GMT-05:00 Indiana (USA)*GMT-04:00 Atlantic Time*GMT-04:00 Caracas, La Paz*GMT-04:00 Santiago*18*GMT-03:00 Brasilia*20*21*GMT-02:00 Mid-Atlantic*23*24*25*GMT London, Dublin, Lisbon*GMT+01:00 Berlin, Rome*GMT+01:00 Budapest, Prague*GMT+01:00 Paris, Madrid*GMT+01:00 Vienna, Warsaw*31*GMT+02:00 Athens, Istanbul*GMT+02:00 Bucharest*GMT+02:00 Cairo*GMT+02:00 Johannesburg, Harare*GMT+02:00 Helsinki, Riga*GMT+02:00 Jerusalem*38*GMT+03:00 Kuwait, Riyadh*GMT+04:00 Moscow*41*GMT+03:30 Tehran*GMT+04:00 Abu Dhabi, Muscat*GMT+04:00 Baku, Tbilisi*45*GMT+06:00 Ekaterinburg*47*GMT+05:30 New Delhi*49*GMT+07:00 Omsk, Novosibirsk *51*52*53*GMT+07:00 Bangkok, Jakarta*GMT+08:00 Krasnoyarsk*GMT+08:00 Beijing, Hong Kong*GMT+09:00 Irkutsk*GMT+08:00 Kuala Lumpur*GMT+08:00 Perth*GMT+08:00 Taipei*GMT+09:00 Tokyo, Osaka*GMT+09:00 Seoul*GMT+10:00 Yakutsk*GMT+09:30 Adelaide*GMT+09:30 Darwin*GMT+10:00 Brisbane*GMT+10:00 Melbourne, Sydney*68*69*GMT+11:00 Vladivostok*71*GMT+12:00 Auckland, Wellington\",\r\n" + 
            "      \"descriptionName\": \"TimeZone\",\r\n" + 
            "      \"objectName\": \"TimeZone\",\r\n" + 
            "      \"memberName\": \"5040\",\r\n" + 
            "      \"hierarchyName\": \"TimeZone\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF0013EC\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"160100078A\",\r\n" + 
            "      \"descriptionName\": \"SerialNumber\",\r\n" + 
            "      \"objectName\": \"SerialNumber\",\r\n" + 
            "      \"memberName\": \"5100\",\r\n" + 
            "      \"hierarchyName\": \"SerialNumber\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!0083FFFFF0013F4\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"'10010'B\",\r\n" + 
            "      \"descriptionName\": \"Device Features\",\r\n" + 
            "      \"objectName\": \"Device Features\",\r\n" + 
            "      \"memberName\": \"5108\",\r\n" + 
            "      \"hierarchyName\": \"Device Features\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!01D00000700001C\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"Downstairs\",\r\n" + 
            "      \"descriptionName\": \"R(1)'Description\",\r\n" + 
            "      \"objectName\": \"R(1)'Description\",\r\n" + 
            "      \"memberName\": \"Description\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'Description\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!10800000000130B\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"192.168.1.1\",\r\n" + 
            "      \"descriptionName\": \"NwkPortIP'IP gefault gateway\",\r\n" + 
            "      \"objectName\": \"NwkPortIP'IP gefault gateway\",\r\n" + 
            "      \"memberName\": \"4875\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortIP'IP gefault gateway\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!10800000000130C\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"255.255.255.0\",\r\n" + 
            "      \"descriptionName\": \"NwkPortIP'IP subnet mask\",\r\n" + 
            "      \"objectName\": \"NwkPortIP'IP subnet mask\",\r\n" + 
            "      \"memberName\": \"4876\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortIP'IP subnet mask\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!10800000000130D\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"192.168.1.41\",\r\n" + 
            "      \"descriptionName\": \"NwkPortIP'IP address\",\r\n" + 
            "      \"objectName\": \"NwkPortIP'IP address\",\r\n" + 
            "      \"memberName\": \"4877\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortIP'IP address\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!10800000000130E\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 47808,\r\n" + 
            "      \"descriptionName\": \"NwkPortIP'UDP Port\",\r\n" + 
            "      \"objectName\": \"NwkPortIP'UDP Port\",\r\n" + 
            "      \"memberName\": \"4878\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortIP'UDP Port\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!108000000001313\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"'F0C77F6C10A1'H\",\r\n" + 
            "      \"descriptionName\": \"NwkPortIP'BACnet MAC address\",\r\n" + 
            "      \"objectName\": \"NwkPortIP'BACnet MAC address\",\r\n" + 
            "      \"memberName\": \"4883\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortIP'BACnet MAC address\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!108000001001286\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"sth.connectivity.ccl-siemens.com\",\r\n" + 
            "      \"descriptionName\": \"NwkPortCCL'Connection URI\",\r\n" + 
            "      \"objectName\": \"NwkPortCCL'Connection URI\",\r\n" + 
            "      \"memberName\": \"4742\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortCCL'Connection URI\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!108000001001287\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"HABEAS-XXQFD-JRHUP-YNAFG-FNE4Q\",\r\n" + 
            "      \"descriptionName\": \"NwkPortCCL'Activation Key\",\r\n" + 
            "      \"objectName\": \"NwkPortCCL'Activation Key\",\r\n" + 
            "      \"memberName\": \"4743\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortCCL'Activation Key\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!108000001001288\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 60,\r\n" + 
            "      \"descriptionName\": \"NwkPortCCL'Reconection delay\",\r\n" + 
            "      \"objectName\": \"NwkPortCCL'Reconection delay\",\r\n" + 
            "      \"memberName\": \"4744\",\r\n" + 
            "      \"hierarchyName\": \"NwkPortCCL'Reconection delay\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!DPUpdCurMon\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 3685,\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Item Updates Current Month\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!DPUpdPerMin\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 1,\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Item Updates per Minute\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!DPUpdTotal\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 429857,\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Item Updates Total\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!Languages\": {\r\n" + 
            "      \"rep\": 0,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": \"-;en\",\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        1\r\n" + 
            "      ],\r\n" + 
            "      \"objectName\": \"CSL-Config\",\r\n" + 
            "      \"memberName\": \"Languages\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!Online\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 1,\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        1\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Offline*Online\",\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Online\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!TrfcInCurMon\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 6836970,\r\n" + 
            "      \"descr\": \"bytes\",\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Traffic Inbound Current Month\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!TrfcInPerMin\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 183,\r\n" + 
            "      \"descr\": \"bytes\",\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Traffic Inbound per Minute\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!TrfcInTotal\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 361410891,\r\n" + 
            "      \"descr\": \"bytes\",\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Traffic Inbound Total\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!TrfcOutCurMon\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 1641440,\r\n" + 
            "      \"descr\": \"bytes\",\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Traffic Outbound Current Month\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!TrfcOutPerMin\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 62,\r\n" + 
            "      \"descr\": \"bytes\",\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Traffic Outbound per Minute\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;0!TrfcOutTotal\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": 105878276,\r\n" + 
            "      \"descr\": \"bytes\",\r\n" + 
            "      \"descriptionName\": \"Target\",\r\n" + 
            "      \"objectName\": \"Target\",\r\n" + 
            "      \"memberName\": \"Traffic Outbound Total\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!00000000E000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 21.4816036,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": -50,\r\n" + 
            "        \"maxValue\": 80\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        -50,\r\n" + 
            "        80\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Outside air temperature\",\r\n" + 
            "      \"objectName\": \"R(1)'TOa\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'TOa\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!00200007F000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 24,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 17,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 50\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        50\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Comfort heating setpoint\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHCmf\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'SpHCmf\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!002000080000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 24,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 50\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        50\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Economy heating setpoint\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHPcf\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'SpHPcf\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!002000081000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 16,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 50\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        50\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Unoccupied heating setpoint\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHEco\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'SpHEco\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!002000082000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 6,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 50\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        50\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Protection heating setpoint\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'TCtlH'SpHPrt\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'SpHPrt\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!002000083000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 16,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 15,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 6,\r\n" + 
            "        \"maxValue\": 35\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        6,\r\n" + 
            "        35\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Room temperature setpoint\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'SpTRDtr'SpTR\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'SpTR\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!002000084000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 0,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 15,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": -10,\r\n" + 
            "        \"maxValue\": 19\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        -18,\r\n" + 
            "        11\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"K\",\r\n" + 
            "      \"descriptionName\": \"Room temperature setpoint shift\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'SpTRDtr'SpTRShft\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'SpTRShft\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.10000000149011612,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!002000085000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 36.1050224,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 100\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        100\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"%r.H.\",\r\n" + 
            "      \"descriptionName\": \"Relative humidity for room\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'RHuRel\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'RHuRel\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 1,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!002000086000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 24.2482586,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 50\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        50\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Room temperature\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'RTemp\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'RTemp\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.10000000149011612,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!0020000B2000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 35,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 50\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        50\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"�C\",\r\n" + 
            "      \"descriptionName\": \"Max. heating setpoint\",\r\n" + 
            "      \"objectName\": \"R(1)'SpTRMaxHCmf\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'SpTRMaxHCmf\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!0020000B4000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 30,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 120\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        120\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Unknown Unit(236)\",\r\n" + 
            "      \"descriptionName\": \"Warm-up gradient\",\r\n" + 
            "      \"objectName\": \"R(1)'WarmUpGrdnt\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'WarmUpGrdnt\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!0020000B5000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 2,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": -5,\r\n" + 
            "        \"maxValue\": 5\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        -5,\r\n" + 
            "        5\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"K\",\r\n" + 
            "      \"descriptionName\": \"Built-in temp. sensor adj.\",\r\n" + 
            "      \"objectName\": \"R(1)'TRBltnMsvAdj\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'TRBltnMsvAdj\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!0020000C6000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 0,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 5\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        5\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"A\",\r\n" + 
            "      \"descriptionName\": \"Heating device electrical load\",\r\n" + 
            "      \"objectName\": \"R(1)'HDevElLd\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'HDevElLd\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!0020000CB000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 0,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 5\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        5\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"A\",\r\n" + 
            "      \"descriptionName\": \"Q22/Q24 electrical load\",\r\n" + 
            "      \"objectName\": \"R(1)'Q22Q24ElLd\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'Q22Q24ElLd\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 0.5,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!0020000CD000055\": {\r\n" + 
            "      \"rep\": 1,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 585.4,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 5000\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        5000\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"ppm\",\r\n" + 
            "      \"descriptionName\": \"Room air quality\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'RAQual\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'RAQual\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"resolution\": 100,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!005000038000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 0,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 13,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Inactive*Active*Null\",\r\n" + 
            "      \"descriptionName\": \"Temporary comfort button\",\r\n" + 
            "      \"objectName\": \"R(1)'ROpModDtr'TmpCmfBtn\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'TmpCmfBtn\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!005000039000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 0,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 13,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Inactive*Active*Null\",\r\n" + 
            "      \"descriptionName\": \"Comfort button\",\r\n" + 
            "      \"objectName\": \"R(1)'ROpModDtr'CmfBtn\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'CmfBtn\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!00500003B000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 0,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Absent*Present*Null\",\r\n" + 
            "      \"descriptionName\": \"Room presence detection\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'RPscDet\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'RPscDet\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!00500003F000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 1,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 17,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"No*Yes*Null\",\r\n" + 
            "      \"descriptionName\": \"Enable heating control\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'TCtlH'EnHCtl\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'EnHCtl\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!005000054000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 0,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Inactive*Active*Null\",\r\n" + 
            "      \"descriptionName\": \"Room presence detector\",\r\n" + 
            "      \"objectName\": \"R(1)'EnRPscDet\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'EnRPscDet\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!01300004C000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 2,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Off*Protection\",\r\n" + 
            "      \"descriptionName\": \"Off/protection configuration\",\r\n" + 
            "      \"objectName\": \"R(1)'OffPrtCnf\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'OffPrtCnf\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!013000051000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 2,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 13,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        3\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Off*Absent*Present\",\r\n" + 
            "      \"descriptionName\": \"Occupancy mode\",\r\n" + 
            "      \"objectName\": \"R(1)'ROpModDtr'OccMod\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'OccMod\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!013000052000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 5,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 15,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        5\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Undefined*Poor*Satisfactory*Good*Excellent\",\r\n" + 
            "      \"descriptionName\": \"Energy efficiency indication room\",\r\n" + 
            "      \"objectName\": \"R(1)'RGrnLf'REei\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'REei\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!013000053000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 1,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 15,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Off*On\",\r\n" + 
            "      \"descriptionName\": \"Domestic hot water mode\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'DhwOp'DhwMod\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'DhwMod\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!013000056000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 1,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        3\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Neither*Heating*Cooling\",\r\n" + 
            "      \"descriptionName\": \"Heating/cooling state\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'HCStaDtr'HCSta\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'HCSta\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!01300005A000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 2,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"presentPriority\": 13,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        4\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Protection*Unoccupied*Economy*Comfort\",\r\n" + 
            "      \"descriptionName\": \"Present operating mode and reason\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'PrOpModRsn\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'PrOpModRsn\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!013000071000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 6,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        6\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Default*Slow*Medium*Fast*2-position*Self-adaptive\",\r\n" + 
            "      \"descriptionName\": \"Heating control loop\",\r\n" + 
            "      \"objectName\": \"R(1)'HCtrSet\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'HCtrSet\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!013000072000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 2,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Warm-up gradient*Self-adaptive\",\r\n" + 
            "      \"descriptionName\": \"Optimum start control setting\",\r\n" + 
            "      \"objectName\": \"R(1)'OsscSet\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'OsscSet\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!013000074000055\": {\r\n" + 
            "      \"rep\": 3,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": false,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 4,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        4\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"Null*Undefined*Poor*Okay*Good\",\r\n" + 
            "      \"descriptionName\": \"Room air quality indication\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'RAQualInd\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'RAQualInd\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!030000000000055\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 500,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 1,\r\n" + 
            "        \"maxValue\": 8760\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        1,\r\n" + 
            "        8760\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"h\",\r\n" + 
            "      \"descriptionName\": \"Pump/valve kick cycle\",\r\n" + 
            "      \"objectName\": \"R(1)'KickCyc\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'KickCyc\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!030000001000055\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 180000,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 3600000\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        3600000\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"ms\",\r\n" + 
            "      \"descriptionName\": \"DHW min. ON time\",\r\n" + 
            "      \"objectName\": \"R(1)'BoDhwTiOnMin\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'BoDhwTiOnMin\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!030000002000055\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 180000,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 3600000\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        3600000\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"ms\",\r\n" + 
            "      \"descriptionName\": \"DHW min. OFF time\",\r\n" + 
            "      \"objectName\": \"R(1)'BoDhwTiOffMin\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrAplSet'BoDhwTiOffMin\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;1!030000007000055\": {\r\n" + 
            "      \"rep\": 2,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": 604800,\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0,\r\n" + 
            "        \"eventState\": 0,\r\n" + 
            "        \"minValue\": 0,\r\n" + 
            "        \"maxValue\": 4294967295\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        4294967295\r\n" + 
            "      ],\r\n" + 
            "      \"descr\": \"min\",\r\n" + 
            "      \"descriptionName\": \"Operating hours heating\",\r\n" + 
            "      \"objectName\": \"R(1)'RHvacCoo'OphHCDtr'OphH\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'OphH\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;2!011000004000055\": {\r\n" + 
            "      \"rep\": 8,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": {\r\n" + 
            "          \"cmd\": \"Command\",\r\n" + 
            "          \"cal\": null,\r\n" + 
            "          \"act\": 0,\r\n" + 
            "          \"null\": 3,\r\n" + 
            "          \"default\": 0,\r\n" + 
            "          \"schedStart\": 4294967295,\r\n" + 
            "          \"schedEnd\": 4294967295,\r\n" + 
            "          \"days\": [\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                3\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7687,\r\n" + 
            "                4\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                3\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                3\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7687,\r\n" + 
            "                4\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                3\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                3\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7687,\r\n" + 
            "                4\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                3\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                3\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7687,\r\n" + 
            "                4\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                3\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                3\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7687,\r\n" + 
            "                4\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                3\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                3\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7687,\r\n" + 
            "                4\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                3\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                3\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7687,\r\n" + 
            "                4\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                3\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            []\r\n" + 
            "          ],\r\n" + 
            "          \"timeTillNextValue\": 509,\r\n" + 
            "          \"nextValue\": 3\r\n" + 
            "        },\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        4\r\n" + 
            "      ],\r\n" + 
            "      \"descriptionName\": \"Room operating mode scheduler\",\r\n" + 
            "      \"objectName\": \"R(1)'CenOpMod'ROpModSched\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'ROpModSched\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;2!011000005000055\": {\r\n" + 
            "      \"rep\": 8,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"value\": {\r\n" + 
            "        \"value\": {\r\n" + 
            "          \"cmd\": \"Command\",\r\n" + 
            "          \"cal\": null,\r\n" + 
            "          \"act\": 0,\r\n" + 
            "          \"null\": 3,\r\n" + 
            "          \"default\": 0,\r\n" + 
            "          \"schedStart\": 4294967295,\r\n" + 
            "          \"schedEnd\": 4294967295,\r\n" + 
            "          \"days\": [\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                9,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7689,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                21,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                1\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                9,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7689,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                21,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                1\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                9,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7689,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                21,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                1\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                9,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7689,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                21,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                1\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                9,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7689,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                21,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                1\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                9,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7689,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                21,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                1\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            [\r\n" + 
            "              [\r\n" + 
            "                0,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                9,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7689,\r\n" + 
            "                1\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                21,\r\n" + 
            "                2\r\n" + 
            "              ],\r\n" + 
            "              [\r\n" + 
            "                7701,\r\n" + 
            "                1\r\n" + 
            "              ]\r\n" + 
            "            ],\r\n" + 
            "            []\r\n" + 
            "          ],\r\n" + 
            "          \"timeTillNextValue\": 479,\r\n" + 
            "          \"nextValue\": 2\r\n" + 
            "        },\r\n" + 
            "        \"statusFlags\": 0,\r\n" + 
            "        \"reliability\": 0\r\n" + 
            "      },\r\n" + 
            "      \"limits\": [\r\n" + 
            "        0,\r\n" + 
            "        2\r\n" + 
            "      ],\r\n" + 
            "      \"descriptionName\": \"Domestic hot water scheduler\",\r\n" + 
            "      \"objectName\": \"R(1)'CenDhw'DhwSched\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'DhwSched\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;3!01100000400004E\": {\r\n" + 
            "      \"rep\": 9,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"descriptionName\": \"Room operating mode scheduler\",\r\n" + 
            "      \"objectName\": \"R(1)'CenOpMod'ROpModSched\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'ROpModSched\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    },\r\n" + 
            "    \"Pfaf770c8-abeb-4742-ad65-ead39030d369;3!01100000500004E\": {\r\n" + 
            "      \"rep\": 9,\r\n" + 
            "      \"type\": 0,\r\n" + 
            "      \"write\": true,\r\n" + 
            "      \"webhookEnabled\": false,\r\n" + 
            "      \"descriptionName\": \"Domestic hot water scheduler\",\r\n" + 
            "      \"objectName\": \"R(1)'CenDhw'DhwSched\",\r\n" + 
            "      \"memberName\": \"PresentValue\",\r\n" + 
            "      \"hierarchyName\": \"R(1)'FvrBscOp'DhwSched\",\r\n" + 
            "      \"translated\": false,\r\n" + 
            "      \"isVirtual\": false\r\n" + 
            "    }\r\n" + 
            "  }\r\n" + 
            "}";
    //@formatter:on

    //@formatter:off
    private static final String DATAPOINTS_JSON_REFRESH_SET =
            "{\"totalCount\":11,"
                    + "\"values\":{"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!Online\":{\"value\":1},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!00000000E000055\":{\"value\":{\"value\":12.6014862,\"statusFlags\":0,\"reliability\":0,\"eventState\":0,\"minValue\":-50.0,\"maxValue\":80.0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000083000055\":{\"value\":{\"value\":16.0,\"statusFlags\":0,\"reliability\":0,\"presentPriority\":15,\"eventState\":0,\"minValue\":6.0,\"maxValue\":35.0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000085000055\":{\"value\":{\"value\":39.1304474,\"statusFlags\":0,\"reliability\":0,\"eventState\":0,\"minValue\":0.0,\"maxValue\":100.0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000086000055\":{\"value\":{\"value\":21.51872,\"statusFlags\":0,\"reliability\":0,\"eventState\":0,\"minValue\":0.0,\"maxValue\":50.0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000051000055\":{\"value\":{\"value\":2,\"statusFlags\":0,\"reliability\":0,\"presentPriority\":13,\"eventState\":0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000052000055\":{\"value\":{\"value\":5,\"statusFlags\":0,\"reliability\":0,\"presentPriority\":15,\"eventState\":0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000053000055\":{\"value\":{\"value\":2,\"statusFlags\":0,\"reliability\":0,\"presentPriority\":15,\"eventState\":0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000056000055\":{\"value\":{\"value\":1,\"statusFlags\":0,\"reliability\":0,\"eventState\":0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!01300005A000055\":{\"value\":{\"value\":2,\"statusFlags\":0,\"reliability\":0,\"presentPriority\":13,\"eventState\":0}},"
                        + "\"Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000074000055\":{\"value\":{\"value\":4,\"statusFlags\":0,\"reliability\":0,\"eventState\":0}}"
                    + "}"
            + "}";
    //@formatter:on

    //@formatter:off
    private static final String PLANTS_JSON = 
        "{" + 
        "    \"totalCount\": 2," + 
        "    \"items\": [{" + 
        "        \"id\": \"Pd1774247-7de7-4896-ac76-b7e0dd943c40\"," + 
        "        \"activationKey\": \"this-is-not-a-valid-activation-key\"," + 
        "        \"address\": \"\"," + 
        "        \"alarmStatus\": 0," + 
        "        \"applicationSetDescription\": \"Siemens Smart Thermostat\\r\\nRDS110 => Device ID 45\\r\\n\"," + 
        "        \"applicationSetId\": \"9964755b-6766-40bd-ba45-77b2446b71bb\"," + 
        "        \"applicationSetName\": \"STH-Default-RDS110\"," + 
        "        \"asn\": \"RDS110\"," + 
        "        \"assigned\": true," + 
        "        \"city\": \"\"," + 
        "        \"country\": \"\"," + 
        "        \"description\": \"\"," + 
        "        \"energyIndicator\": 0," + 
        "        \"isOnline\": true," + 
        "        \"name\": \"this-is-not-a-valid-activation-key-RDS110\"," + 
        "        \"phone\": \"\"," + 
        "        \"serialNumber\": \"this-is-not-a-valid-activation-key\"," + 
        "        \"state\": \"\"," + 
        "        \"taskStatus\": 0," + 
        "        \"tenant\": \"Siemens STH\"," + 
        "        \"tenantId\": \"T290ea1c1-902c-4c0b-9dce-f96119bc7fc1\"," + 
        "        \"timezone\": \"\"," + 
        "        \"zipCode\": \"\"," + 
        "        \"imsi\": \"\"," + 
        "        \"customerPlantId\": null," + 
        "        \"enhancedPrivileges\": false" + 
        "    }, {" + 
        "        \"id\": \"Pfaf770c8-abeb-4742-ad65-ead39030d369\"," + 
        "        \"activationKey\": \"this-is-not-a-valid-activation-key\"," + 
        "        \"address\": \"\"," + 
        "        \"alarmStatus\": 0," + 
        "        \"applicationSetDescription\": \"Siemens Smart Thermostat\\r\\nRDS110 => Device ID 45\\r\\n\"," + 
        "        \"applicationSetId\": \"9964755b-6766-40bd-ba45-77b2446b71bb\"," + 
        "        \"applicationSetName\": \"STH-Default-RDS110\"," + 
        "        \"asn\": \"RDS110\"," + 
        "        \"assigned\": true," + 
        "        \"city\": \"\"," + 
        "        \"country\": \"\"," + 
        "        \"description\": \"\"," + 
        "        \"energyIndicator\": 0," + 
        "        \"isOnline\": true," + 
        "        \"name\": \"this-is-not-a-valid-activation-key-RDS110\"," + 
        "        \"phone\": \"\"," + 
        "        \"serialNumber\": \"this-is-not-a-valid-activation-key\"," + 
        "        \"state\": \"\"," + 
        "        \"taskStatus\": 0," + 
        "        \"tenant\": \"Siemens STH\"," + 
        "        \"tenantId\": \"T290ea1c1-902c-4c0b-9dce-f96119bc7fc1\"," + 
        "        \"timezone\": \"\"," + 
        "        \"zipCode\": \"\"," + 
        "        \"imsi\": \"\"," + 
        "        \"customerPlantId\": null," + 
        "        \"enhancedPrivileges\": false" + 
        "    }]" + 
        "}";
    //@formatter:on

    //@formatter:off
    private static final String ACCESS_TOKEN_JSON = 
        "{" + 
        "    \"access_token\": \"this-is-not-a-valid-access_token\"," + 
        "    \"token_type\": \"bearer\"," + 
        "    \"expires_in\": 1209599," + 
        "    \"userName\": \"software@whitebear.ch\"," + 
        "    \".issued\": \"Thu, 06 Jun 2019 10:27:50 GMT\"," + 
        "    \".expires\": \"Thu, 20 Jun 2019 10:27:50 GMT\"" + 
        "}";
    //@formatter:on

    @Test
    public void test() {
        confirmDegreeSymbolCodingNotTrashed();
        testAccessToken();
        testRdsPlants();
        testRdsDataPointsFull();
        testRdsDataPointsFullNew();
        testRdsDataPointsRefresh();
    }

    private void testRdsDataPointsFullNew() {
        RdsDataPoints dataPoints = RdsDataPoints.createFromJson(DATAPOINTS_JSON_FULL_SET_NEW);
        assertNotNull(dataPoints);
        try {
            assertEquals("Downstairs", dataPoints.getDescription());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }

        @Nullable
        Map<String, BasePoint> points = dataPoints.points;
        assertNotNull(points);
        assertEquals(70, points.size());
    }

    private void confirmDegreeSymbolCodingNotTrashed() {
        /*
         * note: temperature symbols with a degree sign: the MVN Spotless trashes the
         * "degree" (looks like *) symbol, so we must escape these symbols as octal \260
         * or unicode \u00B00 - the following test will indicate is all is ok
         */
        assertTrue("\260C".equals(BasePoint.DEGREES_CELSIUS));
        assertTrue("\u00B0C".equals(BasePoint.DEGREES_CELSIUS));
        assertTrue("\260F".equals(BasePoint.DEGREES_FAHRENHEIT));
        assertTrue("\u00B0F".equals(BasePoint.DEGREES_FAHRENHEIT));
        assertTrue(BasePoint.DEGREES_FAHRENHEIT.startsWith(BasePoint.DEGREES_CELSIUS.substring(0, 1)));
    }

    private void testRdsDataPointsRefresh() {
        RdsDataPoints refreshPoints = RdsDataPoints.createFromJson(DATAPOINTS_JSON_REFRESH_SET);
        assertNotNull(refreshPoints);

        assertNotNull(refreshPoints.points);
        Map<String, BasePoint> refreshMap = refreshPoints.points;
        assertNotNull(refreshMap);

        @Nullable
        BasePoint point;
        State state;

        // check the parsed values
        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;0!Online");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(1, ((DecimalType) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!00000000E000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(12.60, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000083000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(16.0, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000085000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(39.13, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!002000086000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(21.51, ((QuantityType<?>) state).floatValue(), 0.01);

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000051000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(2, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000052000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(5, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000053000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(2, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000056000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(1, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!01300005A000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(2, ((QuantityType<?>) state).intValue());

        point = refreshMap.get("Pd1774247-7de7-4896-ac76-b7e0dd943c40;1!013000074000055");
        assertTrue(point instanceof BasePoint);
        state = point.getState();
        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(4, ((QuantityType<?>) state).intValue());

        RdsDataPoints originalPoints = RdsDataPoints.createFromJson(DATAPOINTS_JSON_FULL_SET);
        assertNotNull(originalPoints);
        assertNotNull(originalPoints.points);

        // check that the refresh point types match the originals
        Map<String, BasePoint> originalMap = originalPoints.points;
        assertNotNull(originalMap);
        @Nullable
        BasePoint refreshPoint;
        @Nullable
        BasePoint originalPoint;
        for (String key : refreshMap.keySet()) {
            refreshPoint = refreshMap.get(key);
            assertTrue(refreshPoint instanceof BasePoint);
            originalPoint = originalMap.get(key);
            assertTrue(originalPoint instanceof BasePoint);
            assertEquals(refreshPoint.getState().getClass(), originalPoint.getState().getClass());
        }
    }

    private void testAccessToken() {
        RdsAccessToken accessToken = RdsAccessToken.createFromJson(ACCESS_TOKEN_JSON);
        assertNotNull(accessToken);
        try {
            assertEquals("this-is-not-a-valid-access_token", accessToken.getToken());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }
        assertTrue(accessToken.isExpired());
    }

    private void testRdsDataPointsFull() {
        RdsDataPoints dataPoints = RdsDataPoints.createFromJson(DATAPOINTS_JSON_FULL_SET);
        assertNotNull(dataPoints);
        try {
            assertEquals("Upstairs", dataPoints.getDescription());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }

        @Nullable
        Map<String, BasePoint> points = dataPoints.points;
        assertNotNull(points);
        assertEquals(67, points.size());

        try {
            assertEquals("AAS-20:SU=SiUn;APT=HvacFnct18z_A;APTV=2.003;APS=1;",
                    dataPoints.getPointByClass("ApplicationSoftwareVersion").getState().toString());
            assertEquals("Device object", dataPoints.getPointByClass("Device Description").getState().toString());
            assertEquals("FW=02.32.02.27;SVS-300.1:SBC=13.22;I",
                    dataPoints.getPointByClass("FirmwareRevision").getState().toString());
            assertEquals("RDS110", dataPoints.getPointByClass("ModelName").getState().toString());
            assertEquals(0, dataPoints.getPointByClass("SystemStatus").asInt());
            assertEquals(0, dataPoints.getPointByClass("UtcOffset").asInt());
            assertEquals(19, dataPoints.getPointByClass("DatabaseRevision").asInt());
            assertEquals(0, dataPoints.getPointByClass("LastRestartReason").asInt());
            assertEquals("MDL:ASN= RDS110;HW=0.2.0;",
                    dataPoints.getPointByClass("ModelInformation").getState().toString());
            assertEquals(1, dataPoints.getPointByClass("Active SystemLanguge").asInt());
            assertEquals(26, dataPoints.getPointByClass("TimeZone").asInt());
            assertEquals("160100096D", dataPoints.getPointByClass("SerialNumber").getState().toString());
            assertEquals("'10010'B", dataPoints.getPointByClass("Device Features").getState().toString());
            assertEquals("Upstairs", dataPoints.getPointByClass("'Description").getState().toString());
            assertEquals("192.168.1.1", dataPoints.getPointByClass("'IP gefault gateway").getState().toString());
            assertEquals("255.255.255.0", dataPoints.getPointByClass("'IP subnet mask").getState().toString());
            assertEquals("192.168.1.42", dataPoints.getPointByClass("'IP address").getState().toString());
            assertEquals(47808, dataPoints.getPointByClass("'UDP Port").asInt());
            assertEquals("'F0C77F6C1895'H", dataPoints.getPointByClass("'BACnet MAC address").getState().toString());
            assertEquals("sth.connectivity.ccl-siemens.com",
                    dataPoints.getPointByClass("'Connection URI").getState().toString());
            assertEquals("this-is-not-a-valid-activation-key",
                    dataPoints.getPointByClass("'Activation Key").getState().toString());
            assertEquals(60, dataPoints.getPointByClass("'Reconection delay").asInt());
            assertEquals(0, dataPoints.getPointByClass("#Item Updates per Minute").asInt());
            assertEquals(286849, dataPoints.getPointByClass("#Item Updates Total").asInt());
            assertEquals("-;en", dataPoints.getPointByClass("#Languages").getState().toString());
            assertEquals(1, dataPoints.getPointByClass("#Online").asInt());
            assertEquals(1473, dataPoints.getPointByClass("#Traffic Inbound per Minute").asInt());
            assertEquals(178130801, dataPoints.getPointByClass("#Traffic Inbound Total").asInt());
            assertEquals(616, dataPoints.getPointByClass("#Traffic Outbound per Minute").asInt());
            assertEquals(60624666, dataPoints.getPointByClass("#Traffic Outbound Total").asInt());
            assertEquals(0, dataPoints.getPointByClass("#Item Updates per Minute").asInt());

            State state;
            QuantityType<?> celsius;

            state = dataPoints.getPointByClass("'TOa").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(18.55, celsius.floatValue(), 0.01);

            assertEquals("0.0", dataPoints.getPointByClass("'HDevElLd").getState().toString());

            state = dataPoints.getPointByClass("'SpHPcf").getState();
            assertTrue(state instanceof QuantityType<?>);
            QuantityType<?> fahrenheit = ((QuantityType<?>) state).toUnit(ImperialUnits.FAHRENHEIT);
            assertNotNull(fahrenheit);
            assertEquals(24.00, fahrenheit.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpHEco").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(16.00, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpHPrt").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(6.00, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpTR").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(24.00, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpTRShft").getState();
            assertTrue(state instanceof QuantityType<?>);
            QuantityType<?> kelvin = ((QuantityType<?>) state).toUnit(Units.KELVIN);
            assertNotNull(kelvin);
            assertEquals(0, kelvin.floatValue(), 0.01);

            assertEquals("46.86865", dataPoints.getPointByClass("'RHuRel").getState().toString());

            state = dataPoints.getPointByClass("'RTemp").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(23.76, celsius.floatValue(), 0.01);

            state = dataPoints.getPointByClass("'SpTRMaxHCmf").getState();
            assertTrue(state instanceof QuantityType<?>);
            celsius = ((QuantityType<?>) state).toUnit(SIUnits.CELSIUS);
            assertNotNull(celsius);
            assertEquals(35.00, celsius.floatValue(), 0.01);

            assertEquals("30.0", dataPoints.getPointByClass("'WarmUpGrdnt").getState().toString());

            state = dataPoints.getPointByClass("'TRBltnMsvAdj").getState();
            assertTrue(state instanceof QuantityType<?>);
            kelvin = ((QuantityType<?>) state).toUnit(Units.KELVIN);
            assertNotNull(kelvin);
            assertEquals(35.0, celsius.floatValue(), 0.01);

            assertEquals("0.0", dataPoints.getPointByClass("'Q22Q24ElLd").getState().toString());
            assertEquals("713.0", dataPoints.getPointByClass("'RAQual").getState().toString());
            assertEquals("0.0", dataPoints.getPointByClass("'TmpCmfBtn").getState().toString());
            assertEquals("0.0", dataPoints.getPointByClass("'CmfBtn").getState().toString());
            assertEquals("0.0", dataPoints.getPointByClass("'RPscDet").getState().toString());
            assertEquals("1.0", dataPoints.getPointByClass("'EnHCtl").getState().toString());
            assertEquals("0.0", dataPoints.getPointByClass("'EnRPscDet").getState().toString());
            assertEquals("2.0", dataPoints.getPointByClass("'OffPrtCnf").getState().toString());
            assertEquals("3.0", dataPoints.getPointByClass("'OccMod").getState().toString());
            assertEquals("5.0", dataPoints.getPointByClass("'REei").getState().toString());
            assertEquals("2.0", dataPoints.getPointByClass("'DhwMod").getState().toString());
            assertEquals("2.0", dataPoints.getPointByClass("'HCSta").getState().toString());
            assertEquals("4.0", dataPoints.getPointByClass("'PrOpModRsn").getState().toString());
            assertEquals("6.0", dataPoints.getPointByClass("'HCtrSet").getState().toString());
            assertEquals("2.0", dataPoints.getPointByClass("'OsscSet").getState().toString());
            assertEquals("4.0", dataPoints.getPointByClass("'RAQualInd").getState().toString());
            assertEquals("500.0", dataPoints.getPointByClass("'KickCyc").getState().toString());
            assertEquals("180000.0", dataPoints.getPointByClass("'BoDhwTiOnMin").getState().toString());
            assertEquals("180000.0", dataPoints.getPointByClass("'BoDhwTiOffMin").getState().toString());
            assertEquals("UNDEF", dataPoints.getPointByClass("'ROpModSched").getState().toString());
            assertEquals("UNDEF", dataPoints.getPointByClass("'DhwSched").getState().toString());
            assertEquals("UNDEF", dataPoints.getPointByClass("'ROpModSched").getState().toString());
            assertEquals("UNDEF", dataPoints.getPointByClass("'DhwSched").getState().toString());
            assertEquals("253140.0", dataPoints.getPointByClass("'OphH").getState().toString());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }

        // test for a missing element
        State test = null;
        try {
            test = dataPoints.getPointByClass("missing-element").getState();
            fail("expected exception did not occur");
        } catch (RdsCloudException e) {
            assertEquals(null, test);
        }

        try {
            // test the all-the-way-round lookup loop
            assertNotNull(dataPoints.points);
            Map<String, BasePoint> pointsMap = dataPoints.points;
            assertNotNull(pointsMap);
            @Nullable
            Object point;
            for (Entry<String, BasePoint> entry : pointsMap.entrySet()) {
                point = entry.getValue();
                assertTrue(point instanceof BasePoint);
                // ignore UNDEF points where all-the-way-round lookup fails
                if (!"UNDEF".equals(((BasePoint) point).getState().toString())) {
                    @Nullable
                    String x = entry.getKey();
                    assertNotNull(x);
                    String y = ((BasePoint) point).getPointClass();
                    String z = dataPoints.pointClassToId(y);
                    assertEquals(x, z);
                }
            }

            State state = null;

            // test the specific points that we use
            state = dataPoints.getPointByClass(HIE_DESCRIPTION).getState();
            assertEquals("Upstairs", state.toString());

            state = dataPoints.getPointByClass(HIE_ROOM_TEMP).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(23.761879, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_OUTSIDE_TEMP).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(18.55, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_TARGET_TEMP).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(24, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_ROOM_HUMIDITY).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(46.86, ((QuantityType<?>) state).floatValue(), 0.01);

            state = dataPoints.getPointByClass(HIE_ROOM_AIR_QUALITY).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Good", state.toString());
            assertEquals("Good", dataPoints.getPointByClass(HIE_ROOM_AIR_QUALITY).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_ENERGY_SAVINGS_LEVEL).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Excellent", state.toString());
            assertEquals("Excellent", dataPoints.getPointByClass(HIE_ENERGY_SAVINGS_LEVEL).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_OUTPUT_STATE).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Heating", state.toString());
            assertEquals("Heating", dataPoints.getPointByClass(HIE_OUTPUT_STATE).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(3, ((QuantityType<?>) state).intValue());
            assertEquals(3, dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).asInt());

            state = dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Present", state.toString());
            assertEquals("Present", dataPoints.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(2, ((QuantityType<?>) state).intValue());
            assertEquals(2, dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).asInt());

            state = dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("On", state.toString());
            assertEquals("On", dataPoints.getPointByClass(HIE_DHW_OUTPUT_STATE).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(4, ((QuantityType<?>) state).intValue());
            assertEquals(4, dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).asInt());

            state = dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Comfort", state.toString());
            assertEquals("Comfort", dataPoints.getPointByClass(HIE_PR_OP_MOD_RSN).getEnum().toString());

            state = dataPoints.getPointByClass(HIE_STAT_CMF_BTN).getState();
            assertEquals(state.getClass(), QuantityType.class);
            assertEquals(0, ((QuantityType<?>) state).intValue());
            assertEquals(0, dataPoints.getPointByClass(HIE_STAT_CMF_BTN).asInt());

            state = dataPoints.getPointByClass(HIE_STAT_CMF_BTN).getEnum();
            assertEquals(state.getClass(), StringType.class);
            assertEquals("Inactive", state.toString());
            assertEquals("Inactive", dataPoints.getPointByClass(HIE_STAT_CMF_BTN).getEnum().toString());

            // test online code
            assertTrue(dataPoints.isOnline());

            // test present priority code
            assertEquals(15, dataPoints.getPointByClass(HIE_TARGET_TEMP).getPresentPriority());

            // test temperature units code (C)
            BasePoint tempPoint = dataPoints.getPointByClass("'SpTR");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(SIUnits.CELSIUS, ((BasePoint) tempPoint).getUnit());

            // test temperature units code (F)
            tempPoint = dataPoints.getPointByClass("'SpHPcf");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(ImperialUnits.FAHRENHEIT, ((BasePoint) tempPoint).getUnit());

            // test temperature units code (K)
            tempPoint = dataPoints.getPointByClass("'SpHPcf");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(ImperialUnits.FAHRENHEIT, ((BasePoint) tempPoint).getUnit());

            tempPoint = dataPoints.getPointByClass("'SpTRShft");
            assertTrue(tempPoint instanceof BasePoint);
            assertEquals(Units.KELVIN, ((BasePoint) tempPoint).getUnit());
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }
    }

    @SuppressWarnings("null")
    private void testRdsPlants() {
        try {
            RdsPlants plants = RdsPlants.createFromJson(PLANTS_JSON);
            assertNotNull(plants);

            List<PlantInfo> plantList = plants.getPlants();
            assertNotNull(plantList);

            Object plant;
            plant = plantList.get(0);
            assertTrue(plant instanceof PlantInfo);
            assertEquals("Pd1774247-7de7-4896-ac76-b7e0dd943c40", ((PlantInfo) plant).getId());
            assertTrue(((PlantInfo) plant).isOnline());

            plant = plantList.get(1);
            assertTrue(plant instanceof PlantInfo);
            assertEquals("Pfaf770c8-abeb-4742-ad65-ead39030d369", ((PlantInfo) plant).getId());
            assertTrue(((PlantInfo) plant).isOnline());
            ;
        } catch (RdsCloudException e) {
            fail(e.getMessage());
        }
    }
}
