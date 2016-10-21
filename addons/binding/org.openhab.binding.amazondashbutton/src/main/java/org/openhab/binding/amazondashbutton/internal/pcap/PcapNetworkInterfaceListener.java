package org.openhab.binding.amazondashbutton.internal.pcap;

import org.pcap4j.core.PcapNetworkInterface;

/**
 * The {@link PcapNetworkInterfaceListener} is notified whenever a {@link PcapNetworkInterface} is added or removed. A
 * {@link PcapNetworkInterfaceListener} can be registered by calling
 * {@link PcapNetworkInterfaceService#registerListener(PcapNetworkInterfaceListener)} and can be unregistered by calling
 * {@link PcapNetworkInterfaceService#unregisterListener(PcapNetworkInterfaceListener)}.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public interface PcapNetworkInterfaceListener {

    /**
     * This method is called whenever a new {@link PcapNetworkInterface} is added.
     *
     * @param newNetworkInterface The added networkInterface
     */
    public void onPcapNetworkInterfaceAdded(PcapNetworkInterface newNetworkInterface);

    /**
     * This method is called whenever a {@link PcapNetworkInterface} is removed.
     *
     * @param removedNetworkInterface The removed networkInterface
     */
    public void onPcapNetworkInterfaceRemoved(PcapNetworkInterface removedNetworkInterface);
}
