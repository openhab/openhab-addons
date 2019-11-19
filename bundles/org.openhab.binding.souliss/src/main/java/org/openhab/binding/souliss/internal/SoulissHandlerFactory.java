/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal;

import static org.openhab.binding.souliss.SoulissBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.handler.SoulissT11Handler;
import org.openhab.binding.souliss.handler.SoulissT12Handler;
import org.openhab.binding.souliss.handler.SoulissT13Handler;
import org.openhab.binding.souliss.handler.SoulissT14Handler;
import org.openhab.binding.souliss.handler.SoulissT16Handler;
import org.openhab.binding.souliss.handler.SoulissT18Handler;
import org.openhab.binding.souliss.handler.SoulissT19Handler;
import org.openhab.binding.souliss.handler.SoulissT1AHandler;
import org.openhab.binding.souliss.handler.SoulissT22Handler;
import org.openhab.binding.souliss.handler.SoulissT31Handler;
import org.openhab.binding.souliss.handler.SoulissT41Handler;
import org.openhab.binding.souliss.handler.SoulissT42Handler;
import org.openhab.binding.souliss.handler.SoulissT51Handler;
import org.openhab.binding.souliss.handler.SoulissT52Handler;
import org.openhab.binding.souliss.handler.SoulissT53Handler;
import org.openhab.binding.souliss.handler.SoulissT54Handler;
import org.openhab.binding.souliss.handler.SoulissT55Handler;
import org.openhab.binding.souliss.handler.SoulissT56Handler;
import org.openhab.binding.souliss.handler.SoulissT57Handler;
import org.openhab.binding.souliss.handler.SoulissT61Handler;
import org.openhab.binding.souliss.handler.SoulissT62Handler;
import org.openhab.binding.souliss.handler.SoulissT63Handler;
import org.openhab.binding.souliss.handler.SoulissT64Handler;
import org.openhab.binding.souliss.handler.SoulissT65Handler;
import org.openhab.binding.souliss.handler.SoulissT66Handler;
import org.openhab.binding.souliss.handler.SoulissT67Handler;
import org.openhab.binding.souliss.handler.SoulissT68Handler;
import org.openhab.binding.souliss.handler.SoulissTopicsHandler;
import org.openhab.binding.souliss.internal.discovery.SoulissGatewayDiscovery;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissHandlerFactory} is responsible for creating things and thing
 * handlers. It fire when a new thing is added.
 *
 * @author Tonino Fazio - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.souliss")
public class SoulissHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(SoulissGatewayDiscovery.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.toString().equals(GATEWAY_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for Gateway '{}'", thingTypeUID);
            // get last byte of IP number
            Configuration gwConfigurationMap = thing.getConfiguration();
            String IPAddressOnLAN = (String) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS);
            Integer i = Integer.parseInt(IPAddressOnLAN.split("\\.")[3]);
            SoulissBindingNetworkParameters.addGateway(i.byteValue(), thing);
            return new SoulissGatewayHandler((Bridge) thing);
        } else if (thingTypeUID.toString().equals(T11_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T11 '{}'", thingTypeUID);
            return new SoulissT11Handler(thing);
        } else if (thingTypeUID.toString().equals(T12_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T12 '{}'", thingTypeUID);
            return new SoulissT12Handler(thing);
        } else if (thingTypeUID.toString().equals(T13_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T13 '{}'", thingTypeUID);
            return new SoulissT13Handler(thing);
        } else if (thingTypeUID.toString().equals(T14_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T14 '{}'", thingTypeUID);
            return new SoulissT14Handler(thing);
        } else if (thingTypeUID.toString().equals(T16_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T16 '{}'", thingTypeUID);
            return new SoulissT16Handler(thing);
        } else if (thingTypeUID.toString().equals(T18_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T18 '{}'", thingTypeUID);
            return new SoulissT18Handler(thing);
        } else if (thingTypeUID.toString().equals(T19_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T19 '{}'", thingTypeUID);
            return new SoulissT19Handler(thing);
        } else if (thingTypeUID.toString().equals(T1A_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T1A '{}'", thingTypeUID);
            return new SoulissT1AHandler(thing);
        } else if (thingTypeUID.toString().equals(T21_THING_TYPE.getAsString().toLowerCase())
                || (thingTypeUID.toString().equals(T22_THING_TYPE.getAsString().toLowerCase()))) {
            logger.debug("Create handler for T21/T22 '{}'", thingTypeUID);
            return new SoulissT22Handler(thing);
        } else if (thingTypeUID.toString().equals(T31_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T31 '{}'", thingTypeUID);
            return new SoulissT31Handler(thing);
        } else if (thingTypeUID.toString().equals(T41_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T41 '{}'", thingTypeUID);
            return new SoulissT41Handler(thing);
        } else if (thingTypeUID.toString().equals(T42_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T42 '{}'", thingTypeUID);
            return new SoulissT42Handler(thing);
        } else if (thingTypeUID.toString().equals(T51_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T51 '{}'", thingTypeUID);
            return new SoulissT51Handler(thing);
        } else if (thingTypeUID.toString().equals(T52_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T52 '{}'", thingTypeUID);
            return new SoulissT52Handler(thing);
        } else if (thingTypeUID.toString().equals(T53_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T53 '{}'", thingTypeUID);
            return new SoulissT53Handler(thing);
        } else if (thingTypeUID.toString().equals(T54_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T54 '{}'", thingTypeUID);
            return new SoulissT54Handler(thing);
        } else if (thingTypeUID.toString().equals(T55_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T55 '{}'", thingTypeUID);
            return new SoulissT55Handler(thing);
        } else if (thingTypeUID.toString().equals(T56_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T56 '{}'", thingTypeUID);
            return new SoulissT56Handler(thing);
        } else if (thingTypeUID.toString().equals(T57_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T57 '{}'", thingTypeUID);
            return new SoulissT57Handler(thing);
        } else if (thingTypeUID.toString().equals(T61_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T61 '{}'", thingTypeUID);
            return new SoulissT61Handler(thing);
        } else if (thingTypeUID.toString().equals(T62_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T62 '{}'", thingTypeUID);
            return new SoulissT62Handler(thing);
        } else if (thingTypeUID.toString().equals(T63_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T63 '{}'", thingTypeUID);
            return new SoulissT63Handler(thing);
        } else if (thingTypeUID.toString().equals(T64_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T64 '{}'", thingTypeUID);
            return new SoulissT64Handler(thing);
        } else if (thingTypeUID.toString().equals(T65_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T65 '{}'", thingTypeUID);
            return new SoulissT65Handler(thing);
        } else if (thingTypeUID.toString().equals(T66_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T66 '{}'", thingTypeUID);
            return new SoulissT66Handler(thing);
        } else if (thingTypeUID.toString().equals(T67_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T67 '{}'", thingTypeUID);
            return new SoulissT67Handler(thing);
        } else if (thingTypeUID.toString().equals(T68_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for T68 '{}'", thingTypeUID);
            return new SoulissT68Handler(thing);
        } else if (thingTypeUID.toString().equals(TOPICS_THING_TYPE.getAsString().toLowerCase())) {
            logger.debug("Create handler for Action Messages (Topics) '{}'", thingTypeUID);
            SoulissBindingNetworkParameters.addTopics(thing.getUID().getAsString().split(":")[2], thing);
            return new SoulissTopicsHandler(thing);
        }

        return null;
    }
}
