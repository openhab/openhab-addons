/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal;

import java.util.List;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 * A simple utitlity class which encapsulates {@link Pcaps} and which catches checked exceptions and transforms them
 * into {@link RuntimeException}s.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class PcapUtil {

    private static final Logger logger = LoggerFactory.getLogger(PcapUtil.class);

    /**
     * Returns all pcap network interfaces relying on {@link Pcaps#findAllDevs()}. The list is filtered as all
     * interfaces which are not bound to an address are excluded.
     *
     * @return An {@link Iterable} of all {@link PcapNetworkInterface}s which are bound to an address
     */
    public static Iterable<PcapNetworkInterface> getBoundNetworkInterfaces() {
        try {
            final List<PcapNetworkInterface> allNetworkInterfaces = Pcaps.findAllDevs();
            return Iterables.filter(allNetworkInterfaces, new Predicate<PcapNetworkInterface>() {

                @Override
                public boolean apply(PcapNetworkInterface networkInterface) {
                    final boolean suitable = !networkInterface.getAddresses().isEmpty();
                    if (!suitable) {
                        logger.debug("{} is not a suitable network interfaces as no addresses are bound to it.",
                                networkInterface.getName());
                    }
                    return suitable;
                }
            });
        } catch (PcapNativeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the pcap network interface with the given name. If no interface is found, null is returned.
     *
     * @param name The name of the pcap network interface
     * @return The network interface with the given name. Returns null, if no interface is found
     */
    public static PcapNetworkInterface getNetworkInterfaceByName(String name) {
        try {
            return Pcaps.getDevByName(name);
        } catch (PcapNativeException e) {
            throw new RuntimeException(e);
        }
    }
}
