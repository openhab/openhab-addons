/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;

import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

/**
 * Class that implement (and register) this interface receive update events from the MySensors network.
 * Default (Java8) added to allow the class that will implement this interface, to choose
 * only the method in which is interested (not best practice, but help code reading).
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public interface MySensorsGatewayEventListener extends EventListener {

    /**
     * Triggered when gateway reserve (and send) an ID for a new network device.
     * A new ,empty, device is created before this method is triggered
     */
    public default void nodeIdReservationDone(Integer reservedId) throws Exception {
    }

    /**
     * Triggered when new node ID is discovered in the network
     * A new ,empty, device is created before this method is triggered. Only if presentation message received, and so a
     * description for a child is available, @child is not null.
     */
    public default void newNodeDiscovered(MySensorsNode node, MySensorsChild child) throws Exception {
    }

    /**
     * When a message of type SET has processed correctly (node/child/variable found in gateway)
     * the new value is sent to every observer. The @updateType parameter could be set to:
     * -REVERT to indicate that channel update was triggered after unsuccessful message sending (when ACK=1)
     * -UPDATE when incoming/outgoing message is received/sent to update state of a variable
     * -BATTERY indicate that a battery update message was received for a node ( @child and @variable are null in this
     * case)
     */
    public default void sensorUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var,
            MySensorsNodeUpdateEventType updateType) throws Exception {
    }

    /**
     * When a node is not more reachable this method is triggered.
     * Reachability changes when connection go down or NetworkSanityChecker tells us the
     * device is not responding
     */
    public default void nodeReachStatusChanged(MySensorsNode node, boolean reach) throws Exception {
    }

    /**
     * Procedure to notify new message from MySensorsNetwork.
     * Internally, MySensorsGateway, handle this event and update channel state if message is sent to a known node.
     */
    public default void messageReceived(MySensorsMessage message) throws Exception {
    }

    /**
     * Triggered when connection update its status
     */
    public default void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected)
            throws Exception {
    }

    /**
     * Triggered when one message, that request ack, hasn't received any confirmation.
     * Internally, MySensorsGateway, handle this event and restore channel state.
     */
    public default void ackNotReceived(MySensorsMessage msg) throws Exception {
    }
}
