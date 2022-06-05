/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.panamaxfurman.internal.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectivityListener;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedListener;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;

/**
 * Represents a connection mechanism (transport) to a remote Power Conditioner device.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public interface PanamaxFurmanTransport {

    /**
     * Add a listener which will be notified when connectivity is established or broken to the Power Conditioner.
     */
    public void addConnectivityListener(PanamaxFurmanConnectivityListener listener);

    /**
     * Add a listener which will be notified when an information message is received ("pushed") from the Power
     * Conditioner.
     */
    public void addInformationReceivedListener(PanamaxFurmanInformationReceivedListener listener);

    /**
     * Send a query to the Power Conditioner, requesting the current state of the given OH channel
     *
     * @return true if the request was able to be sent
     */
    public boolean requestStatusOf(String channelString);

    /**
     * Send a query to the Power Conditioner, requesting the current state of the given OH channel
     *
     * @return true if the request was able to be sent
     */
    public default boolean requestStatusOf(ChannelUID channelUID) {
        return requestStatusOf(channelUID.getId());
    }

    /**
     * Send an update command to the Power Conditioner based on the openHAB command
     */
    public boolean sendUpdateCommand(ChannelUID channelUID, Command command);

    /**
     * @return the name of this connection
     */
    public String getConnectionName();

    /**
     * Close the connection transport and all associated resources
     */
    public void shutdown();
}
