/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal.discovery;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.enigma2.Enigma2BindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Enigma2DiscoveryParticipant} is responsible processing the
 * results of searches for mDNS services of type _ssh._tcp.local. and finding a webinterface
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Enigma2DiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(Enigma2DiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Enigma2BindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo info) {
        DiscoveryResult result = null;
        ThingUID uid = getThingUID(info);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(4);
            String label = info.getName();

            properties.put(Enigma2BindingConstants.DEVICE_PARAMETER_HOST, getIPAddress(info));
            properties.put(Enigma2BindingConstants.DEVICE_PARAMETER_REFRESH, new BigDecimal(5));
            return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo info) {
        logger.debug("ServiceInfo: {}", info);
        if (info != null && getServiceType().equals(info.getType())) {
            logger.trace("Discovered a Enigma2 device thing with name '{}'", info.getName());
            String formatedIP = getFormattedIPAddress(info);
            if (formatedIP != null) {
                return new ThingUID(Enigma2BindingConstants.THING_TYPE_DEVICE, new String(formatedIP));
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        // I know, this is not optimal, but currently i have no better idea!
        return "_ssh._tcp.local.";
    }

    private boolean isEnigma2Device(String ip) {
        String content;
        try {
            content = HttpUtil.executeUrl("GET", "http://" + ip + "/web/about", 5000);
        } catch (IOException e) {
            return false;
        }
        return content != null && content.contains("e2enigmaversion");
    }

    private String getIPAddress(ServiceInfo info) {
        if (info != null) {
            InetAddress[] addrs = info.getInetAddresses();
            List<Inet4Address> listOfInt4Addresses = new ArrayList<>();

            for (int i = 0; i < addrs.length; i++) {
                if (addrs[i] instanceof Inet4Address) {
                    listOfInt4Addresses.add((Inet4Address) addrs[i]);
                }
            }

            if (listOfInt4Addresses.size() > 1) {
                logger.info("Enigma2 device {} reports multiple addresses - using the first one! {}", info.getName(),
                        Arrays.toString(listOfInt4Addresses.toArray(new Inet4Address[listOfInt4Addresses.size()])));
            }

            if (listOfInt4Addresses.size() > 0) {
                String ip = listOfInt4Addresses.get(0).getHostAddress();
                if (isEnigma2Device(ip)) {
                    return ip;
                }
            }
        }
        return null;
    }

    private String getFormattedIPAddress(ServiceInfo info) {
        String ip = getIPAddress(info);
        if (ip != null) {
            String formatedIP = ip.replace(".", "");
            return formatedIP;
        }
        return null;
    }
}
