package org.openhab.binding.amazondashbutton.internal;

import java.util.List;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

public class PcapUtil {

    public static List<PcapNetworkInterface> getAllNetworkInterfaces() {
        try {
            return Pcaps.findAllDevs();
        } catch (PcapNativeException e) {
            throw new RuntimeException(e);
        }
    }
}
