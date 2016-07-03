/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.concurrent.TimeUnit;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveDelayedPollEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Hail command class. Some devices handle state changes by 'hailing' the controller and asking it to
 * request the device values
 *
 * @author Chris Jackson
 * @author Ben Jones
 */
@XStreamAlias("hailCommandClass")
public class ZWaveHailCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveHailCommandClass.class);

    private static final int HAIL = 1;

    /**
     * Creates a new instance of the ZWaveHailCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveHailCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.HAIL;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Hail command (v{})", this.getNode().getNodeId(), this.getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case HAIL:
                logger.debug("NODE {}: Request an update of the dynamic values", this.getNode().getNodeId());

                // We only re-request dynamic values for nodes that are completely initialized.
                if (this.getNode().getNodeState() != ZWaveNodeState.ALIVE
                        || this.getNode().isInitializationComplete() == false) {
                    return;
                }

                // Send delayed poll event
                getController().notifyEventListeners(
                        new ZWaveDelayedPollEvent(this.getNode().getNodeId(), 0, 75, TimeUnit.MILLISECONDS));

                break;
            default:
                logger.warn("NODE {}: Unsupported Command {} for command class {}.", this.getNode().getNodeId(),
                        command, this.getCommandClass().getLabel());
        }
    }
}
