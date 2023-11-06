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
package org.openhab.binding.tradfri.internal.handler;

import static org.openhab.core.thing.Thing.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tradfri.internal.CoapCallback;
import org.openhab.binding.tradfri.internal.TradfriCoapClient;
import org.openhab.binding.tradfri.internal.config.TradfriDeviceConfig;
import org.openhab.binding.tradfri.internal.model.TradfriDeviceData;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TradfriThingHandler} is the abstract base class for individual device handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Restructuring and refactoring of the binding
 */
@NonNullByDefault
public abstract class TradfriThingHandler extends BaseThingHandler implements CoapCallback {

    private final Logger logger = LoggerFactory.getLogger(TradfriThingHandler.class);

    // the unique instance id of the device
    protected @Nullable Integer id;

    // used to check whether we have already been disposed when receiving data asynchronously
    protected volatile boolean active;

    protected @Nullable TradfriCoapClient coapClient;

    private @Nullable CoapObserveRelation observeRelation;

    public TradfriThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("null")
    public synchronized void initialize() {
        Bridge tradfriGateway = getBridge();
        this.id = getConfigAs(TradfriDeviceConfig.class).id;
        TradfriGatewayHandler handler = (TradfriGatewayHandler) tradfriGateway.getHandler();

        active = true;
        updateStatus(ThingStatus.UNKNOWN);
        switch (tradfriGateway.getStatus()) {
            case ONLINE:
                String uriString = handler.getGatewayURI() + "/" + id;
                try {
                    URI uri = new URI(uriString);
                    coapClient = new TradfriCoapClient(uri);
                    coapClient.setEndpoint(handler.getEndpoint());
                } catch (URISyntaxException e) {
                    logger.debug("Illegal device URI `{}`: {}", uriString, e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                    return;
                }

                scheduler.schedule(() -> {
                    observeRelation = coapClient.startObserve(this);
                }, 3, TimeUnit.SECONDS);
                break;
            case OFFLINE:
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        String.format("Gateway offline '%s'", tradfriGateway.getStatusInfo()));
                break;
        }
    }

    @Override
    public synchronized void dispose() {
        active = false;
        if (observeRelation != null) {
            observeRelation.reactiveCancel();
            observeRelation = null;
        }
        if (coapClient != null) {
            coapClient.shutdown();
        }
        super.dispose();
    }

    @Override
    @SuppressWarnings("null")
    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        if (active && getBridge().getStatus() != ThingStatus.OFFLINE && status != ThingStatus.ONLINE) {
            updateStatus(status, statusDetail);
            // we are offline and lost our observe relation - let's try to establish the connection in 10 seconds again
            scheduler.schedule(() -> {
                if (observeRelation != null) {
                    observeRelation.reactiveCancel();
                    observeRelation = null;
                }
                observeRelation = coapClient.startObserve(this);
            }, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        // the status might have changed because the bridge is completely reconfigured - so we need to re-establish
        // our CoAP connection as well
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            dispose();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initialize();
        }
    }

    protected void set(String payload) {
        TradfriCoapClient coapClient = this.coapClient;
        if (coapClient != null) {
            logger.debug("Sending payload: {}", payload);
            coapClient.asyncPut(payload, this, scheduler);
        } else {
            logger.debug("coapClient is null!");
        }
    }

    protected void updateDeviceProperties(TradfriDeviceData state) {
        String firmwareVersion = state.getFirmwareVersion();
        if (firmwareVersion != null) {
            getThing().setProperty(PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        }

        String modelId = state.getModelId();
        if (modelId != null) {
            getThing().setProperty(PROPERTY_MODEL_ID, modelId);
        }

        String vendor = state.getVendor();
        if (vendor != null) {
            getThing().setProperty(PROPERTY_VENDOR, vendor);
        }
    }
}
