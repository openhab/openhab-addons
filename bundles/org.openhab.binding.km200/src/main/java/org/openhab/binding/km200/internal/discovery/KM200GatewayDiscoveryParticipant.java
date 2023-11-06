/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.km200.internal.discovery;

import static org.openhab.binding.km200.internal.KM200BindingConstants.THING_TYPE_KMDEVICE;

import java.net.InetAddress;
import java.util.Random;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.km200.internal.handler.KM200GatewayHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KM200GatewayDiscoveryParticipant} class discovers gateways and adds the results to the inbox.
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.km200")
public class KM200GatewayDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final Set<ThingTypeUID> SUPPORTED_ALL_THING_TYPES_UIDS = KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS;
    private static final String IP4_ADDRESS = "ip4Address";

    private final Logger logger = LoggerFactory.getLogger(KM200GatewayDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_ALL_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo info) {
        ThingUID uid = getThingUID(info);
        logger.debug("MDNS info: {}, uid: {}", info, uid);
        if (uid != null) {
            InetAddress[] addrs = info.getInetAddresses();
            logger.debug("ip: {} id:{}", addrs[0].getHostAddress(), uid.getId());
            return DiscoveryResultBuilder.create(uid).withProperty(IP4_ADDRESS, addrs[0].getHostAddress())
                    .withProperty("deviceId", uid.getId()).withRepresentationProperty(IP4_ADDRESS)
                    .withLabel("KM50/100/200 Gateway (" + addrs[0].getHostAddress() + ")").build();
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo info) {
        ThingTypeUID typeUID = getThingTypeUID(info);
        if (typeUID != null) {
            logger.debug("getType: {}", info.getType());
            if (info.getType() != null) {
                if (info.getType().equalsIgnoreCase(getServiceType())) {
                    String devId = info.getPropertyString("uuid");
                    if (null != devId) {
                        logger.info("Discovered a KMXXX gateway with name: '{}' id: '{}'", info.getName(), devId);
                        if (devId.isEmpty()) {
                            /* If something is wrong then we are generating a random UUID */
                            logger.debug("Error in automatic device-id detection. Using random value");
                            Random rnd = new Random();
                            devId = String.valueOf(rnd.nextLong());
                        }
                        return new ThingUID(typeUID, devId);
                    } else {
                        logger.debug("No uuid property found");
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    private @Nullable ThingTypeUID getThingTypeUID(ServiceInfo info) {
        InetAddress[] addrs = info.getInetAddresses();
        if (addrs.length > 0) {
            String hardwareID = info.getPropertyString("hwversion");
            logger.debug("hardwareID: {}", hardwareID);
            if (hardwareID != null && hardwareID.contains("iCom_Low")) {
                return THING_TYPE_KMDEVICE;
            }
        }
        return null;
    }
}
