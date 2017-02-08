/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.harmonyhub.HarmonyHubBindingConstants;
import org.openhab.binding.harmonyhub.handler.HarmonyHubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HarmonyHubDiscoveryParticipant} class discovers Harmony hubs and adds the results to the inbox.
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
public class HarmonyHubDiscoveryParticipant extends AbstractDiscoveryService implements HarmonyHubDiscoveryListener {

    private Logger logger = LoggerFactory.getLogger(HarmonyHubDiscoveryParticipant.class);

    private static final int TIMEOUT = 15;
    private static final long REFRESH = 600;
    private ScheduledFuture<?> discoFuture;
    private HarmonyHubDiscovery disco;

    public HarmonyHubDiscoveryParticipant() {
        super(HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        disco = new HarmonyHubDiscovery(TIMEOUT);
        disco.addListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        logger.debug("StartScan called");
        disco.startDiscovery();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#startBackgroundDiscovery()
     */
    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Harmony Hub background discovery");
        if (discoFuture == null || discoFuture.isCancelled()) {
            logger.debug("Start Scan");
            discoFuture = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    startScan();
                }
            }, 0, REFRESH, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop HarmonyHub background discovery");
        if (discoFuture != null && !discoFuture.isCancelled()) {
            discoFuture.cancel(true);
            discoFuture = null;
        }
        disco.stopDiscovery();
    }

    @Override
    public void hubDiscoveryFinished() {
    }

    @Override
    public void hubDiscovered(HarmonyHubDiscoveryResult result) {
        logger.trace("Adding HarmonyHub {} ({}) at host {}", result.getFriendlyName(), result.getId(),
                result.getHost());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put("name", result.getFriendlyName());
        ThingUID uid = new ThingUID(HarmonyHubBindingConstants.HARMONY_HUB_THING_TYPE,
                result.getId().replaceAll("[^A-Za-z0-9\\-_]", ""));
        if (uid != null) {
            DiscoveryResult discoResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("HarmonyHub " + result.getFriendlyName()).build();
            thingDiscovered(discoResult);
        }
    }
}
