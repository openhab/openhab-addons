/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal.pcap;

import java.util.List;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

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
     * Returns all pcap network interfaces relying on {@link Pcaps#findAllDevs()}.
     *
     * @return A {@link List} of all {@link PcapNetworkInterface}s
     */
    public static List<PcapNetworkInterface> getAllNetworkInterfaces() {
        try {
            final List<PcapNetworkInterface> allNetworkInterfaces = Pcaps.findAllDevs();
            return allNetworkInterfaces;
        } catch (PcapNativeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the pcap network interface with the given name relying on {@link Pcaps#getDevByName(String)}. If no
     * interface is found, null is returned.
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
