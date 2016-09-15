package org.openhab.binding.amazondashbutton.internal;

import java.util.List;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

/**
 *
 * a simple utitlity class which encapsulates {@link Pcaps} and which catches checked exceptions and transforms them
 * into {@link RuntimeException}s.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class PcapUtil {

    /**
     * Returns all pcap network interfaces
     *
     * @return A list of all {@link PcapNetworkInterface}s
     */
    public static List<PcapNetworkInterface> getAllNetworkInterfaces() {
        try {
            return Pcaps.findAllDevs();
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
