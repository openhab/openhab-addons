/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rio;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;

/**
 * Represents the abstract base to a {@link BaseThingHandler} for common functionality to all Things. This abstract
 * base provides management of the {@link AbstractRioProtocol}, parent {@link #bridgeStatusChanged(ThingStatusInfo)}
 * event processing and the ability to get the current {@link SocketSession}.
 * {@link #sendCommand(String)} and responses will be received on any {@link SocketSessionListener}
 *
 * @author Tim Roberts
 * @version $Id: $Id
 */
public abstract class AbstractThingHandler<E extends AbstractRioProtocol> extends BaseThingHandler {
    /**
     * The retry bridge event - will only be created when we are retrying to find out if the bridge is online
     */
    private ScheduledFuture<?> _retryBridge;

    /**
     * The protocol handler for this base
     */
    private E _protocolHandler;

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
     * @param protocolHandler a, possibly null, {@link AbstractRioProtocol}
     */
    protected void setProtocolHandler(E protocolHandler) {
        if (_protocolHandler != null) {
            _protocolHandler.dispose();
        }
        _protocolHandler = protocolHandler;
    }

    /**
     * Get's the {@link AbstractRioProtocol} handler. May be null if none currently exists
     *
     * @return a {@link AbstractRioProtocol} handler or null if none exists
     */
    protected E getProtocolHandler() {
        return _protocolHandler;
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
     * Overrides the base to try to initialize (via {@link #retryBridge()}) our thing
     */
    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        retryBridge();
        super.bridgeHandlerInitialized(thingHandler, bridge);
    }

    /**
     * Overrides the base to initialize or dispose the handler based on the parent bridge status changing. This will
     * call {@link #initialize()} if the status becomes online. If offline, {@link #dispose()} will be called instead.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            dispose();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        retryBridge();
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    /**
     * Overrides the base to go offline if the parent bridge is disposed
     */
    @Override
    public void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
        dispose();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge disposed of");
        super.bridgeHandlerDisposed(thingHandler, bridge);
    }

    // @Override
    // protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
    // if (status != ThingStatus.INITIALIZING && status != ThingStatus.ONLINE) {
    // retryBridge();
    // }
    // super.updateStatus(status, statusDetail, description);
    // }

    /**
     * Helper method to start checking the status of the bridge and to initialize when the bridge goes online
     */
    private void retryBridge() {
        if (_retryBridge == null) {
            _retryBridge = this.scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    final Bridge bridge = getBridge();
                    if (bridge == null) {
                        _retryBridge.cancel(false);
                        _retryBridge = null;
                    } else {
                        if (bridge.getStatus() == ThingStatus.ONLINE) {
                            if (getThing().getStatus() != ThingStatus.ONLINE) {
                                initialize();
                            }
                            _retryBridge.cancel(false);
                            _retryBridge = null;
                        }
                    }
                }
            }, 250, 1000, TimeUnit.MILLISECONDS);

        }
    }

    /**
     * Overrides the dispose to simply set the protocol handler to null (thereby disposing the existing protocol handler
     * via {@link #setProtocolHandler(AbstractRioProtocol)}
     */
    @Override
    public void dispose() {
        setProtocolHandler(null);
    }
}
