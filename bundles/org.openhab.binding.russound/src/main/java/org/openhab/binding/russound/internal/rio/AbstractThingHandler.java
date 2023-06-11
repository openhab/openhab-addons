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
package org.openhab.binding.russound.internal.rio;

import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * Represents the abstract base to a {@link BaseThingHandler} for common functionality to all Things. This abstract
 * base provides management of the {@link AbstractRioProtocol}, parent {@link #bridgeStatusChanged(ThingStatusInfo)}
 * event processing and the ability to get the current {@link SocketSession}.
 * {@link #sendCommand(String)} and responses will be received on any {@link SocketSessionListener}
 *
 * @author Tim Roberts - Initial contribution
 */
public abstract class AbstractThingHandler<E extends AbstractRioProtocol> extends BaseThingHandler
        implements RioCallbackHandler {
    /**
     * The protocol handler for this base
     */
    private E protocolHandler;

    /**
     * Creates the handler from the given {@link Thing}
     *
     * @param thing a non-null {@link Thing}
     */
    protected AbstractThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Sets a new {@link AbstractRioProtocol} as the current protocol handler. If one already exists, it will be
     * disposed of first.
     *
     * @param newProtocolHandler a, possibly null, {@link AbstractRioProtocol}
     */
    protected void setProtocolHandler(E newProtocolHandler) {
        if (protocolHandler != null) {
            protocolHandler.dispose();
        }
        protocolHandler = newProtocolHandler;
    }

    /**
     * Get's the {@link AbstractRioProtocol} handler. May be null if none currently exists
     *
     * @return an {@link AbstractRioProtocol} handler or null if none exists
     */
    protected E getProtocolHandler() {
        return protocolHandler;
    }

    /**
     * Overridden to simply get the protocol handler's {@link RioHandlerCallback}
     *
     * @return the {@link RioHandlerCallback} or null if not found
     */
    @Override
    public RioHandlerCallback getRioHandlerCallback() {
        final E protocolHandler = getProtocolHandler();
        return protocolHandler == null ? null : protocolHandler.getCallback();
    }

    /**
     * Returns the {@link SocketSession} for this {@link Bridge}. The default implementation is to look in the parent
     * {@link #getBridge()} for the {@link SocketSession}
     *
     * @return a {@link SocketSession} or null if none exists
     */
    @SuppressWarnings("rawtypes")
    protected SocketSession getSocketSession() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof AbstractBridgeHandler) {
            return ((AbstractBridgeHandler) bridge.getHandler()).getSocketSession();
        }
        return null;
    }

    /**
     * Overrides the base to initialize or dispose the handler based on the parent bridge status changing. If offline,
     * {@link #dispose()} will be called instead. We then try to reinitialize ourselves when the bridge goes back online
     * via the {@link #retryBridge()} method.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            reconnect();
        } else {
            disconnect();
        }
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    /**
     * Overrides the base method to remove any state linked to the {@lin ChannelUID} from the
     * {@link StatefulHandlerCallback}
     */
    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // Remove any state when unlinking (that way if it is relinked - we get it)
        final RioHandlerCallback callback = getProtocolHandler().getCallback();
        if (callback instanceof StatefulHandlerCallback) {
            ((StatefulHandlerCallback) callback).removeState(channelUID.getId());
        }
        super.channelUnlinked(channelUID);
    }

    /**
     * Base method to reconnect the handler. The base implementation will simply {@link #disconnect()} then
     * {@link #initialize()} the handler.
     */
    protected void reconnect() {
        disconnect();
        initialize();
    }

    /**
     * Base method to disconnect the handler. This implementation will simply call
     * {@link #setProtocolHandler(AbstractRioProtocol)} to null.
     */
    protected void disconnect() {
        setProtocolHandler(null);
    }

    /**
     * Overrides the dispose to call the {@link #disconnect()} method to disconnect the handler
     */
    @Override
    public void dispose() {
        disconnect();
        super.dispose();
    }
}
