/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.message.sequencers;

import org.openhab.binding.cul.max.internal.messages.BaseMsg;

/**
 * This creates an interface for Message Sequencers. They allow you to run
 * through a state machine depending on the messages that are received.
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
public interface MessageSequencer {

    /**
     * Main call to sequencer
     *
     * @param msg
     *            Latest received message
     */
    void runSequencer(BaseMsg msg);

    /**
     * Handle case where packet is lost
     *
     * @param msg
     *            Message to retransmit
     */
    void packetLost(BaseMsg msg);

    /**
     * Query if the message sequence is complete
     *
     * @return true of is completed and can be discarded
     */
    boolean isComplete();

    /**
     * Returns true if we are at a point in the sequence where we can use fast
     * send (i.e. no wakeup)
     *
     * @return true if we can use fast send
     */
    boolean useFastSend();
}
