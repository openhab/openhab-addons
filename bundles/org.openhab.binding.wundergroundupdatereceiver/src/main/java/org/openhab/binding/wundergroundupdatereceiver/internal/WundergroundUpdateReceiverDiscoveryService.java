/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        WundergroundUpdateReceiverDiscoveryService.class }, configurationPid = "discovery.wundergroundupdatereceiver")
public class WundergroundUpdateReceiverDiscoveryService extends AbstractDiscoveryService {

    @Nullable
    WundergroundUpdateReceiverServletControls servletControls;

    private static final int TIMEOUT_SEC = 1;
    private final HashMap<String, Map<String, String>> thinglessStationIds = new HashMap<>();
    private boolean servletWasInactive = false;

    private boolean scanning = false;

    @Activate
    public WundergroundUpdateReceiverDiscoveryService() throws IllegalArgumentException {
        this(true);
    }

    public WundergroundUpdateReceiverDiscoveryService(boolean useBackgroundDiscovery) throws IllegalArgumentException {
        super(WundergroundUpdateReceiverBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT_SEC,
                useBackgroundDiscovery);
    }

    public void removeUnhandledStationId(String stationId) {
        thinglessStationIds.remove(stationId);
    }

    public void addUnhandledStationId(@Nullable String stationId, Map<String, String> request) {
        if (stationId == null || stationId.isEmpty()) {
            return;
        }
        if (!this.thinglessStationIds.containsKey(stationId)) {
            this.thinglessStationIds.put(stationId, request);
            if (isBackgroundDiscoveryEnabled()) {
                createDiscoveryResult(stationId);
            }
        }
    }

    public boolean isDiscovering() {
        return isBackgroundDiscoveryEnabled() || isScanning();
    }

    public @Nullable Map<String, String> getUnhandledStationRequest(@Nullable String stationId) {
        return this.thinglessStationIds.get(stationId);
    }

    private void createDiscoveryResult(String stationId) {
        ThingUID id = new ThingUID(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER, stationId);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(id)
                .withRepresentationProperty(WundergroundUpdateReceiverBindingConstants.REPRESENTATION_PROPERTY)
                .withProperty(WundergroundUpdateReceiverBindingConstants.REPRESENTATION_PROPERTY, stationId)
                .withThingType(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER)
                .withLabel("WundergroundUpdateReceiver ID " + stationId).build();
        this.thingDiscovered(discoveryResult);
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        setScanning(true);
        if (servletControls != null && !servletControls.isActive()) {
            servletWasInactive = true;
            servletControls.enable();
        }
        thinglessStationIds.keySet().forEach(this::createDiscoveryResult);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        thinglessStationIds.keySet().forEach(this::createDiscoveryResult);
        if (!isBackgroundDiscoveryEnabled() && servletControls != null && servletWasInactive) {
            servletControls.disable();
        }
        setScanning(false);
    }

    protected synchronized boolean isScanning() {
        return this.scanning;
    }

    protected synchronized void setScanning(boolean value) {
        this.scanning = value;
    }
}
