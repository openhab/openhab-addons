/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.discovery;

import java.math.BigInteger;
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
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoundTouchDiscoveryParticipant} is responsible processing the
 * results of searches for mDNS services of type _pulse-server._tcp.local.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class SoundTouchDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(SoundTouchDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return BoseSoundTouchBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo info) {
        DiscoveryResult result = null;
        ThingUID uid = getThingUID(info);
        if (uid != null) {

            Map<String, Object> properties = new HashMap<>(2);
            String label = "unnamed Bose SoundTouch device";
            try {
                label = info.getName();
            } catch (Exception e) {
                // ignore and use default label
            }
            // remove the domain from the name
            InetAddress[] addrs = info.getInetAddresses();

            // we expect only one address per device..
            if (addrs.length > 1) {
                logger.warn("Bose SoundTouch device " + info.getName() + " (" + label
                        + ") reports multiple addresses - using the first one!" + Arrays.toString(addrs));
            }

            properties.put(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST, addrs[0].getHostAddress());
            if (getMacAddress(info) != null) {
                properties.put(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_MAC, new String(getMacAddress(info)));
            }
            return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo info) {
        if (info != null) {
            logger.debug("ServiceInfo: " + info);
            if (info.getType() != null) {
                if (info.getType().equals(getServiceType())) {
                    logger.trace("Discovered a Bose SoundTouch thing with name '{}'", info.getName());
                    byte[] mac = getMacAddress(info);
                    if (mac != null) {
                        return new ThingUID(BoseSoundTouchBindingConstants.THING_TYPE_DEVICE, new String(mac));
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
        return "_soundtouch._tcp.local.";
    }

    private byte[] getMacAddress(ServiceInfo info) {
        if (info != null) {
            byte[] mac = info.getPropertyBytes("MAC");
            if (mac == null) {
                logger.warn("SoundTouch Device " + info.getName() + " delivered no MAC Address!");
                return null;
            }
            if (mac.length != 12) {
                BigInteger bi = new BigInteger(1, mac);
                logger.warn("SoundTouch Device " + info.getName() + " delivered an invalid MAC Address: 0x"
                        + String.format("%0" + (mac.length << 1) + "X", bi));
                return null;
            }
            return mac;
        }
        return null;
    }
}
