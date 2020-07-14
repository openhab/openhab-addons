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
package org.openhab.binding.upnpcontrol.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpHandler} is the base class for {@link UpnpRendererHandler} and {@link UpnpServerHandler}.
 *
 * @author Mark Herwege - Initial contribution
 * @author Karel Goderis - Based on UPnP logic in Sonos binding
 */
@NonNullByDefault
public abstract class UpnpHandler extends BaseThingHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(UpnpHandler.class);

    protected UpnpIOService service;
    protected volatile String transportState = "";
    protected volatile int connectionId;
    protected volatile int avTransportId;
    protected volatile int rcsId;
    protected @NonNullByDefault({}) UpnpControlConfiguration config;

    public UpnpHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing);

        upnpIOService.registerParticipant(this);
        this.service = upnpIOService;
    }

    @Override
    public void initialize() {
        config = getConfigAs(UpnpControlConfiguration.class);
        service.registerParticipant(this);
    }

    @Override
    public void dispose() {
        service.unregisterParticipant(this);
    }

    /**
     * Invoke PrepareForConnection on the UPnP Connection Manager.
     * Result is received in {@link onValueReceived}.
     *
     * @param remoteProtocolInfo
     * @param peerConnectionManager
     * @param peerConnectionId
     * @param direction
     */
    protected void prepareForConnection(String remoteProtocolInfo, String peerConnectionManager, int peerConnectionId,
            String direction) {
        HashMap<String, String> inputs = new HashMap<String, String>();
        inputs.put("RemoteProtocolInfo", remoteProtocolInfo);
        inputs.put("PeerConnectionManager", peerConnectionManager);
        inputs.put("PeerConnectionID", Integer.toString(peerConnectionId));
        inputs.put("Direction", direction);

        invokeAction("ConnectionManager", "PrepareForConnection", inputs);
    }

    /**
     * Invoke ConnectionComplete on UPnP Connection Manager.
     *
     * @param connectionId
     */
    protected void connectionComplete(int connectionId) {
        HashMap<String, String> inputs = new HashMap<String, String>();
        inputs.put("ConnectionID", String.valueOf(connectionId));

        invokeAction("ConnectionManager", "ConnectionComplete", inputs);
    }

    /**
     * Invoke GetTransportState on UPnP AV Transport.
     * Result is received in {@link onValueReceived}.
     */
    protected void getTransportState() {
        HashMap<String, String> inputs = new HashMap<String, String>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "GetTransportInfo", inputs);
    }

    /**
     * Invoke GetProtocolInfo on UPnP Connection Manager.
     * Result is received in {@link onValueReceived}.
     */
    protected void getProtocolInfo() {
        Map<String, String> inputs = new HashMap<>();

        invokeAction("ConnectionManager", "GetProtocolInfo", inputs);
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        logger.debug("Upnp device {} received subscription reply {} from service {}", thing.getLabel(), succeeded,
                service);
    }

    @Override
    public void onStatusChanged(boolean status) {
        if (status) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication lost with " + thing.getLabel());
        }
    }

    @Override
    public @Nullable String getUDN() {
        return config.udn;
    }

    /**
     * This method wraps {@link org.eclipse.smarthome.io.transport.upnp.UpnpIOService.invokeAction}. It schedules and
     * submits the call and calls {@link onValueReceived} upon completion. All state updates or other actions depending
     * on the results should be triggered from {@link onValueReceived} because the class fields with results will be
     * filled asynchronously.
     *
     * @param serviceId
     * @param actionId
     * @param inputs
     */
    protected void invokeAction(String serviceId, String actionId, Map<String, String> inputs) {
        scheduler.submit(() -> {
            Map<String, String> result = service.invokeAction(this, serviceId, actionId, inputs);
            if (logger.isDebugEnabled() && !"GetPositionInfo".equals(actionId)) {
                // don't log position info refresh every second
                logger.debug("Upnp device {} invoke upnp action {} on service {} with inputs {}", thing.getLabel(),
                        actionId, serviceId, inputs);
                logger.debug("Upnp device {} invoke upnp action {} on service {} reply {}", thing.getLabel(), actionId,
                        serviceId, result);
            }
            for (String variable : result.keySet()) {
                onValueReceived(variable, result.get(variable), serviceId);
            }
        });
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (variable == null) {
            return;
        }
        switch (variable) {
            case "CurrentTransportState":
                if (!((value == null) || (value.isEmpty()))) {
                    transportState = value;
                }
                break;
            case "ConnectionID":
                connectionId = Integer.parseInt(value);
                break;
            case "AVTransportID":
                avTransportId = Integer.parseInt(value);
                break;
            case "RcsID":
                rcsId = Integer.parseInt(value);
                break;
            default:
                break;
        }
    }

    /**
     * Subscribe this handler as a participant to a GENA subscription.
     *
     * @param serviceId
     * @param duration
     */
    protected void addSubscription(String serviceId, int duration) {
        logger.debug("Upnp device {} add upnp subscription on {}", thing.getLabel(), serviceId);
        service.addSubscription(this, serviceId, duration);
    }

    /**
     * Remove this handler from the GENA subscriptions.
     *
     * @param serviceId
     */
    protected void removeSubscription(String serviceId) {
        if (service.isRegistered(this)) {
            service.removeSubscription(this, serviceId);
        }
    }
}
