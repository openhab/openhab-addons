/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.discovery;

import org.openhab.binding.mysensors.internal.event.MySensorsEventType;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

/**
 * Thread is started for discovery of new things / nodes.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public class DiscoveryThread implements MySensorsUpdateListener {
    private MySensorsBridgeConnection mysCon;
    private MySensorsDiscoveryService mysDiscoServ;

    /**
     * Initialize the discovery thread.
     *
     * @param mysCon Location of the EventListener to receive messages from the MySensors network.
     * @param mysDiscoServ Location of the service that started this thread
     */
    public DiscoveryThread(MySensorsBridgeConnection mysCon, MySensorsDiscoveryService mysDiscoServ) {
        this.mysCon = mysCon;
        this.mysDiscoServ = mysDiscoServ;
    }

    /**
     * Start the discovery process.
     */
    public void start() {
        mysCon.addEventListener(this);
    }

    /**
     * Stop the discovery process.
     */
    public void stop() {
        mysCon.removeEventListener(this);
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        if (event.getEventType() == MySensorsEventType.INCOMING_MESSAGE) {
            mysDiscoServ.newDevicePresented((MySensorsMessage) event.getData());
        }

    }
}
