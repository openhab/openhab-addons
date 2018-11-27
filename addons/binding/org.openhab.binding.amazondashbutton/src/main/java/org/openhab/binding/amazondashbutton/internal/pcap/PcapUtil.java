/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal.pcap;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;

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

    /**
     * Returns all Pcap network interfaces relying on {@link Pcaps#findAllDevs()}.
     *
     * @return A {@link Iterable} of all {@link PcapNetworkInterfaceWrapper}s
     */
    public static Iterable<PcapNetworkInterfaceWrapper> getAllNetworkInterfaces() {
        try {
            final Iterable<PcapNetworkInterfaceWrapper> allNetworkInterfaces = Iterables
                    .transform(Pcaps.findAllDevs(), PcapNetworkInterfaceWrapper.TRANSFORMER);
            return allNetworkInterfaces;
        } catch (PcapNativeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Pcap network interface with the given name relying on {@link Pcaps#getDevByName(String)}. If no
     * interface is found, null is returned.
     *
     * @param name The name of the Pcap network interface
     * @return The network interface with the given name. Returns null, if no interface is found
     */
    public static PcapNetworkInterfaceWrapper getNetworkInterfaceByName(String name) {
        try {
            return PcapNetworkInterfaceWrapper.TRANSFORMER.apply(Pcaps.getDevByName(name));
        } catch (PcapNativeException e) {
            throw new RuntimeException(e);
        }
    }
}
