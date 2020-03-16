/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.connection;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddr;

/**
 * Interface for a {@link StateMachine}. These methods are visible to the states, used by the {@link StateMachine}.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public interface StateContext {
    /**
     * Sets the new state of the StateMachine.
     *
     * @param newStateClass the class of the new State
     */
    void setState(Class<? extends AbstractConnectionState> newStateClass);

    /**
     * Checks if the given State is active by comparing the identity.
     *
     * @param otherState the stat to check
     * @return true, if the given state is active
     */
    boolean isStateActive(AbstractState otherState);

    /**
     * Gets the Connection to the PCK gateway.
     *
     * @return the Connection
     */
    Connection getConnection();

    /**
     * Notifies openHAB the Connection has failed and enters the necessary State to handle the situation.
     *
     * @param e the reason why the Connection has been failed
     */
    void handleConnectionFailed(@Nullable Throwable e);

    /**
     * Enqueues a PCK message. When the Connection is offline, the message will be buffered and sent out as soon as the
     * Connection recovers. The message is discarded if it is too old, when the Connection recovers.
     *
     * @param addr the destination address
     * @param wantsAck true, if the module shall respond with an Ack
     * @param data the PCK message
     */
    void queue(LcnAddr addr, boolean wantsAck, ByteBuffer data);
}
