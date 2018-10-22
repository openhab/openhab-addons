/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.riscocloud.internal.discovery;

import static org.openhab.binding.riscocloud.RiscoCloudBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.riscocloud.handler.RiscoCloudBridgeHandler;
import org.openhab.binding.riscocloud.handler.SiteBridgeHandler;
import org.openhab.binding.riscocloud.json.ServerDatasHandler;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RiscoCloudDiscoveryService} creates things based on the configured location.
 *
 * @author Sébastien Cantineau - Initial Contribution
 */
@NonNullByDefault
public class RiscoCloudDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(RiscoCloudDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;

    private final RiscoCloudBridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> discoveryJob;

    /**
     * Creates a RiscoCloudDiscoveryService with enabled autostart.
     */
    public RiscoCloudDiscoveryService(RiscoCloudBridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, @Nullable Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting RiscoCloud discovery scan");
        createResults();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (discoveryJob == null) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                createResults();
            }, 0, 24, TimeUnit.HOURS);
            logger.debug("Scheduled RiscoCloud-changed job every {} hours", 24);
        }
    }

    private void createResults() {
        logger.debug("createResults()");
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        if (bridgeHandler.getThing().getThingTypeUID().equals(LOGIN_BRIDGE_THING_TYPE)) {
            bridgeHandler.getSiteList().forEach((idSite, nameSite) -> {
                ThingUID siteThing = new ThingUID(SITE_BRIDGE_THING_TYPE, idSite.toString());
                Map<String, Object> bridgeProperties = new HashMap<>();
                bridgeProperties.put(SITE_ID, idSite);
                bridgeProperties.put(SITE_NAME, nameSite);
                thingDiscovered(DiscoveryResultBuilder.create(siteThing).withLabel("Site bridge " + nameSite)
                        .withProperties(bridgeProperties).withRepresentationProperty(idSite.toString())
                        .withBridge(bridgeUID).build());
            });
        } else if (bridgeHandler.getThing().getThingTypeUID().equals(SITE_BRIDGE_THING_TYPE)) {
            ServerDatasHandler datas = ((SiteBridgeHandler) bridgeHandler).getServerDatasHandler();
            if (datas != null) {
                // Create Overview Thing

                ThingUID overViewThing = new ThingUID(OVERVIEW_THING_TYPE, bridgeHandler.getThing().getUID(),
                        "overview");
                // Map<String, Object> properties = new HashMap<>();
                // properties.put(SITE_ID, idSite);
                // properties.put(SITE_NAME, nameSite);
                thingDiscovered(DiscoveryResultBuilder.create(overViewThing)
                        .withLabel(((SiteBridgeHandler) bridgeHandler).getSiteName() + " overview")
                        // .withProperties(properties)
                        .withBridge(bridgeHandler.getThing().getUID()).build());

                // Create Parts Things
                datas.getPartList().forEach((id, name) -> {
                    ThingUID partThing = new ThingUID(PART_THING_TYPE, bridgeHandler.getThing().getUID(), "part-" + id);
                    Map<String, Object> partProperties = new HashMap<>();
                    partProperties.put(PART_ID, id);
                    thingDiscovered(DiscoveryResultBuilder.create(partThing)
                            .withLabel(((SiteBridgeHandler) bridgeHandler).getSiteName() + " - " + name)
                            .withProperties(partProperties).withBridge(bridgeHandler.getThing().getUID()).build());
                });
            } else {
                logger.debug("datas is null");
            }
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping RiscoCloud background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            if (discoveryJob.cancel(true)) {
                discoveryJob = null;
                logger.debug("Stopped RiscoCloud background discovery");
            }
        }
    }

}
