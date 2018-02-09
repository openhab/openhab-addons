/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;

import com.google.gson.Gson;

/**
 * Represents the abstract base to a {@link BaseBridgeHandler} for common functionality to all Bridges. This abstract
 * base provides management of the {@link AbstractRioProtocol}, parent {@link #bridgeStatusChanged(ThingStatusInfo)}
 * event processing and the ability to get the current {@link SocketSession}.
 * {@link #sendCommand(String)} and responses will be received on any {@link SocketSessionListener}
 *
 * @author Tim Roberts
 */
public abstract class AbstractBridgeHandler<E extends AbstractRioProtocol> extends BaseBridgeHandler
        implements RioCallbackHandler {
    /**
     * The protocol handler for this base
     */
    private E protocolHandler;

    /**
     * Creates the handler from the given {@link Bridge}
     *
     * @param bridge a non-null {@link Bridge}
     */
    protected AbstractBridgeHandler(Bridge bridge) {
        super(bridge);
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
     * @return a {@link AbstractRioProtocol} handler or null if none exists
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
    public RioHandlerCallback getCallback() {
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
    public SocketSession getSocketSession() {
        final Bridge bridge = getBridge();
        if (bridge.getHandler() instanceof AbstractBridgeHandler) {
            return ((AbstractBridgeHandler) bridge.getHandler()).getSocketSession();
        }
        return null;
    }

    /**
     * Returns the {@link RioPresetsProtocol} for this {@link Bridge}. The default implementation is to look in the
     * parent
     * {@link #getPresetsProtocol()} for the {@link RioPresetsProtocol}
     *
     * @return a {@link RioPresetsProtocol} or null if none exists
     */
    @SuppressWarnings("rawtypes")
    public RioPresetsProtocol getPresetsProtocol() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof AbstractBridgeHandler) {
            return ((AbstractBridgeHandler) bridge.getHandler()).getPresetsProtocol();
        }
        return null;
    }

    /**
     * Returns the {@link RioSystemFavoritesProtocol} for this {@link Bridge}. The default implementation is to look in
     * the
     * parent
     * {@link #getPresetsProtocol()} for the {@link RioSystemFavoritesProtocol}
     *
     * @return a {@link RioSystemFavoritesProtocol} or null if none exists
     */
    @SuppressWarnings("rawtypes")
    public RioSystemFavoritesProtocol getSystemFavoritesHandler() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof AbstractBridgeHandler) {
            return ((AbstractBridgeHandler) bridge.getHandler()).getSystemFavoritesHandler();
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
     * Creates an "{id:x, name: 'xx'}" json string from all {@link RioNamedHandler} for a specific class and sends that
     * result to a channel id.
     *
     * @param gson a non-null {@link Gson} to use
     * @param clazz a non-null class that the results will be for
     * @param channelId a non-null, non-empty channel identifier to send the results to
     * @throws IllegalArgumentException if any argument is null or empty
     */
    protected <H extends RioNamedHandler> void refreshNamedHandler(Gson gson, Class<H> clazz, String channelId) {
        if (gson == null) {
            throw new IllegalArgumentException("gson cannot be null");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }
        if (StringUtils.isEmpty(channelId)) {
            throw new IllegalArgumentException("channelId cannot be null or empty");
        }

        final List<IdName> ids = new ArrayList<IdName>();
        for (Thing thn : getThing().getThings()) {
            if (thn.getStatus() == ThingStatus.ONLINE) {
                final ThingHandler handler = thn.getHandler();
                if (handler != null && handler.getClass().isAssignableFrom(clazz)) {
                    final RioNamedHandler namedHandler = (RioNamedHandler) handler;
                    if (namedHandler.getId() > 0) { // 0 returned when handler is initializing
                        ids.add(new IdName(namedHandler.getId(), namedHandler.getName()));
                    }
                }
            }
        }

        final String json = gson.toJson(ids);
        updateState(channelId, new StringType(json));
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

    /**
     * Private class that simply stores an ID & Name. This class is solely used to create a json result like "{id:1,
     * name:'stuff'}"
     *
     * @author Tim Roberts
     */
    private class IdName {
        @SuppressWarnings("unused")
        private final int id;
        @SuppressWarnings("unused")
        private final String name;

        public IdName(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
