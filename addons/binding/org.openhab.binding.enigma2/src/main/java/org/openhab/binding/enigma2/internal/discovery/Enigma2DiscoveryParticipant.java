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
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
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
            String label;
            try {
                label = info.getName();
            } catch (Exception e) {
                label = "unnamed enigma2 device";
            }
            // remove the domain from the name
            InetAddress[] addrs = info.getInetAddresses();

            if (addrs.length > 1) {
                logger.warn("Enigma2 device {} ({}) reports multiple addresses - using the first one! {}",
                        info.getName(), label, Arrays.toString(addrs));
            }

            Long defaultRefreshinterval = (long) 5000;
            properties.put(Enigma2BindingConstants.DEVICE_PARAMETER_HOST, addrs[0].getHostAddress());
            properties.put(Enigma2BindingConstants.DEVICE_PARAMETER_USER, "");
            properties.put(Enigma2BindingConstants.DEVICE_PARAMETER_PASSWORD, "");
            properties.put(Enigma2BindingConstants.DEVICE_PARAMETER_REFRESH, defaultRefreshinterval);
            return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo info) {
        if (info != null) {
            logger.debug("ServiceInfo: {}", info);
            if (info.getType() != null) {
                if (info.getType().equals(getServiceType())) {
                    logger.trace("Discovered a Enigma2 device thing with name '{}'", info.getName());
                    String formatedIP = getFormattedIPAddress(info);
                    if (formatedIP != null) {
                        return new ThingUID(Enigma2BindingConstants.THING_TYPE_DEVICE, new String(formatedIP));
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        // I know, this is not optimal, but currently i have no better idea!
        return "_ssh._tcp.local.";
    }

    private boolean isIPValid(String ip) {
        String content;
        try {
            content = HttpUtil.executeUrl("GET", "http://" + ip + "/web/about", 5000);
        } catch (IOException e) {
            return false;
        }
        if ((content != null) && (content.contains("e2enigmaversion"))) {
            return true;
        }
        return false;
    }

    private String getFormattedIPAddress(ServiceInfo info) {
        if (info != null) {
            InetAddress[] addrs = info.getInetAddresses();
            if (addrs.length > 0) {
                String ip = addrs[0].getHostAddress();
                if (isIPValid(ip)) {
                    if (ip != null) {
                        String formatedIP = ip.replace(".", "");
                        return formatedIP;
                    }
                }
            }
        }
        return null;
    }
}
