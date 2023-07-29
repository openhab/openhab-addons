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
     * This method is called whenever a new {@link PcapNetworkInterfaceWrapper} is added.
     *
     * @param newNetworkInterface The added networkInterface
     */
    public void onPcapNetworkInterfaceAdded(PcapNetworkInterfaceWrapper newNetworkInterface);

    /**
     * This method is called whenever a {@link PcapNetworkInterfaceWrapper} is removed.
     *
     * @param removedNetworkInterface The removed networkInterface
     */
    public void onPcapNetworkInterfaceRemoved(PcapNetworkInterfaceWrapper removedNetworkInterface);
}
