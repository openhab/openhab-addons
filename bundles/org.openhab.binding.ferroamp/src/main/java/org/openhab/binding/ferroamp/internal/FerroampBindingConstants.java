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
package org.openhab.binding.ferroamp.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FerroampBindingConstants} class defines common constants, which are
 * used across the whole binding.
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
    public static final String CHANNEL_EHUBWLOADCONSQL1 = "ehubwloadconsql1";
    public static final String CHANNEL_EHUBWLOADCONSQL2 = "ehubwloadconsql2";
    public static final String CHANNEL_EHUBWLOADCONSQL3 = "ehubwloadconsql3";
    public static final String CHANNEL_EHUBILOADDL1 = "ehubiloaddl1";
    public static final String CHANNEL_EHUBILOADDL2 = "ehubiloaddl2";
    public static final String CHANNEL_EHUBILOADDL3 = "ehubiloaddl3";
    public static final String CHANNEL_EHUBWINVCONSQ_3P = "ehubwinvconsq_3p";
    public static final String CHANNEL_EHUBWEXTCONSQL1 = "ehubwextconsql1";
    public static final String CHANNEL_EHUBWEXTCONSQL2 = "ehubwextconsql2";
    public static final String CHANNEL_EHUBWEXTCONSQL3 = "ehubwextconsql3";
    public static final String CHANNEL_EHUBWINVPRODQ_3P = "ehubwinvprodq_3p";
    public static final String CHANNEL_EHUBWINVCONSQL1 = "ehubwinvconsql1";
    public static final String CHANNEL_EHUBWINVCONSQL2 = "ehubwinvconsql2";
    public static final String CHANNEL_EHUBWINVCONSQL3 = "ehubwinvconsql3";
    public static final String CHANNEL_EHUBIEXTL1 = "ehubiextl1";
    public static final String CHANNEL_EHUBIEXTL2 = "ehubiextl2";
    public static final String CHANNEL_EHUBIEXTL3 = "ehubiextl3";
    public static final String CHANNEL_EHUBILOADQL1 = "ehubiloadql1";
    public static final String CHANNEL_EHUBILOADQL2 = "ehubiloadql2";
    public static final String CHANNEL_EHUBILOADQL3 = "ehubiloadql3";
    public static final String CHANNEL_EHUBWLOADPRODQ_3P = "ehubwloadprodq_3p";
    public static final String CHANNEL_EHUBIACEL1 = "ehubiacel1";
    public static final String CHANNEL_EHUBIACEL2 = "ehubiacel2";
    public static final String CHANNEL_EHUBIACEL3 = "ehubiacel3";
    public static final String CHANNEL_EHUBPLOADL1 = "ehubpLoadl1";
    public static final String CHANNEL_EHUBPLOADL2 = "ehubpLoadl2";
    public static final String CHANNEL_EHUBPLOADL3 = "ehubpLoadl3";
    public static final String CHANNEL_EHUBPINVREACTIVEL1 = "ehubpinvreactivel1";
    public static final String CHANNEL_EHUBPINVREACTIVEL2 = "ehubpinvreactivel2";
    public static final String CHANNEL_EHUBPINVREACTIVEL3 = "ehubpinvreactivel3";
    public static final String CHANNEL_EHUBTS = "ehubts";
    public static final String CHANNEL_EHUBPLOADREACTIVEL1 = "ehubploadreactivel1";
    public static final String CHANNEL_EHUBPLOADREACTIVEL2 = "ehubploadreactivel2";
    public static final String CHANNEL_EHUBPLOADREACTIVEL3 = "ehubploadreactivel3";
    public static final String CHANNEL_EHUBSTATE = "ehubstate";
    public static final String CHANNEL_EHUBWLOADPRODQL1 = "ehubwloadprodql1";
    public static final String CHANNEL_EHUBWLOADPRODQL2 = "ehubwloadprodql2";
    public static final String CHANNEL_EHUBWLOADPRODQL3 = "ehubwloadprodql3";
    public static final String CHANNEL_EHUBPPV = "ehubppv";
    public static final String CHANNEL_EHUBPINVL1 = "ehubpinvl1";
    public static final String CHANNEL_EHUBPINVL2 = "ehubpinvl2";
    public static final String CHANNEL_EHUBPINVL3 = "ehubpinvl3";
    public static final String CHANNEL_EHUBIEXTQL1 = "ehubiextql1";
    public static final String CHANNEL_EHUBIEXTQL2 = "ehubiextql2";
    public static final String CHANNEL_EHUBIEXTQL3 = "ehubiextql3";
    public static final String CHANNEL_EHUBPEXTL1 = "ehubpextl1";
    public static final String CHANNEL_EHUBPEXTL2 = "ehubpextl2";
    public static final String CHANNEL_EHUBPEXTL3 = "ehubpextl3";
    public static final String CHANNEL_EHUBWEXTPRODQL1 = "ehubwextprodql1";
    public static final String CHANNEL_EHUBWEXTPRODQL2 = "ehubwextprodql2";
    public static final String CHANNEL_EHUBWEXTPRODQL3 = "ehubwextprodql3";
    public static final String CHANNEL_EHUBWPV = "ehubwpv";
    public static final String CHANNEL_EHUBPEXTREACTIVEL1 = "ehubpextreactivel1";
    public static final String CHANNEL_EHUBPEXTREACTIVEL2 = "ehubpextreactivel2";
    public static final String CHANNEL_EHUBPEXTREACTIVEL3 = "ehubpextreactivel3";
    public static final String CHANNEL_EHUBUDCPOS = "ehubudcpos";
    public static final String CHANNEL_EHUBUDCNEG = "ehubudcneg";
    public static final String CHANNEL_EHUBSEXT = "ehubsext";
    public static final String CHANNEL_EHUBIEXTDL1 = "ehubiextdl1";
    public static final String CHANNEL_EHUBIEXTDL2 = "ehubiextdl2";
    public static final String CHANNEL_EHUBIEXTDL3 = "ehubiextdl3";
    public static final String CHANNEL_EHUBWEXTCONSQ_3P = "ehubwextconsq_3p";
    public static final String CHANNEL_EHUBILDL1 = "ehubildl1";
    public static final String CHANNEL_EHUBILDL2 = "ehubildl2";
    public static final String CHANNEL_EHUBILDL3 = "ehubildl3";
    public static final String CHANNEL_EHUBGRIDFREQ = "ehubgridfreq";
    public static final String CHANNEL_EHUBWLOADCONSQ_3P = "ehubwloadconsq_3p";
    public static final String CHANNEL_EHUBULL1 = "ehubull1";
    public static final String CHANNEL_EHUBULL2 = "ehubull2";
    public static final String CHANNEL_EHUBULL3 = "ehubull3";
    public static final String CHANNEL_EHUBWEXTPRODQ_3P = "ehubwextprodq_3p";
    public static final String CHANNEL_EHUBILQL1 = "ehubilql1";
    public static final String CHANNEL_EHUBILQL2 = "ehubilql2";
    public static final String CHANNEL_EHUBILQL3 = "ehubilql3";
    public static final String CHANNEL_EHUBWINVPRODQL1 = "ehubwinvprodql1";
    public static final String CHANNEL_EHUBWINVPRODQL2 = "ehubwinvprodql2";
    public static final String CHANNEL_EHUBWINVPRODQL3 = "ehubwinvprodql3";
    public static final String CHANNEL_EHUBILL1 = "ehubill1";
    public static final String CHANNEL_EHUBILL2 = "ehubill2";
    public static final String CHANNEL_EHUBILL3 = "ehubill3";

    // List of battery setup Channel ids
    public static final String CHANNEL_EHUBWBATPROD = "ehubwbatprod";
    public static final String CHANNEL_EHUBWPBATCONS = "ehubwpbatcons";
    public static final String CHANNEL_EHUBSOC = "ehubsoc";
    public static final String CHANNEL_EHUBSOH = "ehubsoh";
    public static final String CHANNEL_EHUBPBAT = "ehubpbat";
    public static final String CHANNEL_EHUBRATEDCAP = "ehubratedcap";

    // List of SSO Channel ids
    public static final String CHANNEL_SSOS0RELAYSTATUS = "ssos0relaystatus";
    public static final String CHANNEL_SSOS0TEMP = "ssos0temp";
    public static final String CHANNEL_SSOS0WPV = "ssos0wpv";
    public static final String CHANNEL_SSOS0TS = "ssos0ts";
    public static final String CHANNEL_SSOS0UDC = "ssos0udc";
    public static final String CHANNEL_SSOS0FAULTCODE = "ssos0faultcode";
    public static final String CHANNEL_SSOS0IPV = "ssos0ipv";
    public static final String CHANNEL_SSOS0UPV = "ssos0upv";
    public static final String CHANNEL_SSOS0ID = "ssos0id";
    public static final String CHANNEL_SSOS1RELAYSTATUS = "ssos0relaystatus";
    public static final String CHANNEL_SSOS1TEMP = "ssos1temp";
    public static final String CHANNEL_SSOS1WPV = "ssos1wpv";
    public static final String CHANNEL_SSOS1TS = "ssos1ts";
    public static final String CHANNEL_SSOS1UDC = "ssos1udc";
    public static final String CHANNEL_SSOS1FAULTCODE = "ssos1faultcode";
    public static final String CHANNEL_SSOS1IPV = "ssos1ipv";
    public static final String CHANNEL_SSOS1UPV = "ssos1upv";
    public static final String CHANNEL_SSOS1ID = "ssos1id";

    public static final String CHANNEL_SSOS2RELAYSTATUS = "ssos2relaystatus";
    public static final String CHANNEL_SSOS2TEMP = "ssos2temp";
    public static final String CHANNEL_SSOS2WPV = "ssos2wpv";
    public static final String CHANNEL_SSOS2TS = "ssos2ts";
    public static final String CHANNEL_SSOS2UDC = "ssos2udc";
    public static final String CHANNEL_SSOS2FAULTCODE = "ssos2faultcode";
    public static final String CHANNEL_SSOS2IPV = "ssos2ipv";
    public static final String CHANNEL_SSOS2UPV = "ssos2upv";
    public static final String CHANNEL_SSOS2ID = "ssos2id";
    public static final String CHANNEL_SSOS3RELAYSTATUS = "ssos3relaystatus";
    public static final String CHANNEL_SSOS3TEMP = "ssos3temp";
    public static final String CHANNEL_SSOS3WPV = "ssos3wpv";
    public static final String CHANNEL_SSOS3TS = "ssos3ts";
    public static final String CHANNEL_SSOS3UDC = "ssos3udc";
    public static final String CHANNEL_SSOS3FAULTCODE = "ssos3faultcode";
    public static final String CHANNEL_SSOS3IPV = "ssos3ipv";
    public static final String CHANNEL_SSOS3UPV = "ssos3upv";
    public static final String CHANNEL_SSOS3ID = "ssos3id";

    // List of ESO Channel ids
    public static final String CHANNEL_ESOFAULTCODE = "esofaultcode";
    public static final String CHANNEL_ESOID = "esoid";
    public static final String CHANNEL_ESOIBAT = "esoibat";
    public static final String CHANNEL_ESOUBAT = "esoubat";
    public static final String CHANNEL_ESORELAYSTATUS = "esorelaystatus";
    public static final String CHANNEL_ESOSOC = "esosoc";
    public static final String CHANNEL_ESOTEMP = "esotemp";
    public static final String CHANNEL_ESOWBATPROD = "esowbatprod";
    public static final String CHANNEL_ESOUDC = "esoudc";
    public static final String CHANNEL_ESOTS = "esots";

    // List of ESM Channel ids
    public static final String CHANNEL_ESMSOH = "esmsoh";
    public static final String CHANNEL_ESMSOC = "esmsoc";
    public static final String CHANNEL_ESMRATEDCAPACITY = "esmratedcapacity";
    public static final String CHANNEL_ESMSID = "esmid";
    public static final String CHANNEL_ESMRATEDPOWER = "esmratedpower";
    public static final String CHANNEL_ESMSTATUS = "esmstatus";
    public static final String CHANNEL_ESMTS = "esmts";

    // List of all Channel ids for configuration
    public static final String CHANNEL_REQUESTCHARGE = "requestcharge";
    public static final String CHANNEL_REQUESTDISCHARGE = "requestdischarge";
    public static final String CHANNEL_AUTO = "requestauto";
    public static final String CHANNEL_REQUESTEXTAPIVERSION = "requestextapiversion";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGYHUB);
}
