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
package org.openhab.binding.windcentrale.internal;

import static org.openhab.binding.windcentrale.internal.WindcentraleBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.windcentrale.internal.api.WindcentraleAPI;
import org.openhab.binding.windcentrale.internal.dto.Project;
import org.openhab.binding.windcentrale.internal.dto.Windmill;
import org.openhab.binding.windcentrale.internal.exception.FailedGettingDataException;
import org.openhab.binding.windcentrale.internal.exception.InvalidAccessTokenException;
import org.openhab.binding.windcentrale.internal.handler.WindcentraleAccountHandler;
import org.openhab.binding.windcentrale.internal.handler.WindcentraleWindmillHandler;
import org.openhab.binding.windcentrale.internal.listener.ThingStatusListener;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WindcentraleDiscoveryService} discovers windmills using the participations in projects provided by the
 * Windcentrale API.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WindcentraleDiscoveryService extends AbstractDiscoveryService
        implements ThingHandlerService, ThingStatusListener {

    private final Logger logger = LoggerFactory.getLogger(WindcentraleDiscoveryService.class);
    private @NonNullByDefault({}) WindcentraleAccountHandler accountHandler;
    private @Nullable Future<?> discoveryJob;

    public WindcentraleDiscoveryService() {
        super(Set.of(THING_TYPE_WINDMILL), 10, false);
    }

    protected void activate(ComponentContext context) {
    }

    @Override
    public void deactivate() {
        cancelDiscoveryJob();
        super.deactivate();
        accountHandler.removeThingStatusListener(this);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof WindcentraleAccountHandler accountHandler) {
            accountHandler.addThingStatusListener(this);
            this.accountHandler = accountHandler;
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Discover windmills (manual discovery)");
        cancelDiscoveryJob();
        discoveryJob = scheduler.submit(this::discoverWindmills);
    }

    @Override
    protected synchronized void stopScan() {
        cancelDiscoveryJob();
        super.stopScan();
    }

    @Override
    public void thingStatusChanged(Thing thing, ThingStatus status) {
        if (ThingStatus.ONLINE.equals(status)) {
            logger.debug("Discover windmills (account online)");
            discoverWindmills();
        }
    }

    private void cancelDiscoveryJob() {
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
        }
    }

    private void discoverWindmills() {
        ThingUID bridgeUID = accountHandler.getThing().getUID();
        WindcentraleAPI api = accountHandler.getAPI();

        if (api == null) {
            logger.debug("Cannot discover windmills because API is null for {}", bridgeUID);
            return;
        }

        logger.debug("Starting discovery scan for {}", bridgeUID);
        try {
            calculateWindmillShares(api.getProjects()).entrySet()
                    .forEach(windmillShares -> addWindmillDiscoveryResult(bridgeUID, windmillShares.getKey(),
                            windmillShares.getValue()));
        } catch (FailedGettingDataException | InvalidAccessTokenException e) {
            logger.debug("Exception during discovery scan for {}", bridgeUID, e);
        }
        logger.debug("Finished discovery scan for {}", bridgeUID);
    }

    private Map<Windmill, Integer> calculateWindmillShares(List<Project> projects) {
        Map<Windmill, Integer> windmillShares = new HashMap<>();

        for (Project project : projects) {
            Windmill windmill = Windmill.fromProjectCode(project.projectCode);
            if (windmill != null) {
                int shares = Objects.requireNonNullElse(windmillShares.get(windmill), 0);
                shares += project.participations.stream()
                        .collect(Collectors.summingInt(participation -> participation.share));
                windmillShares.put(windmill, shares);
            } else {
                logger.debug("Unsupported project code: {}", project.projectCode);
            }
        }

        return windmillShares;
    }

    private void addWindmillDiscoveryResult(ThingUID bridgeUID, Windmill windmill, int shares) {
        String deviceId = windmill.getName().toLowerCase().replace(" ", "-");
        ThingUID thingUID = new ThingUID(THING_TYPE_WINDMILL, bridgeUID, deviceId);

        thingDiscovered(DiscoveryResultBuilder.create(thingUID) //
                .withThingType(THING_TYPE_WINDMILL) //
                .withLabel(windmill.getName()) //
                .withBridge(bridgeUID) //
                .withProperty(PROPERTY_NAME, windmill.getName()) //
                .withProperty(PROPERTY_SHARES, shares) //
                .withProperties(new HashMap<>(WindcentraleWindmillHandler.getWindmillProperties(windmill))) //
                .withRepresentationProperty(PROPERTY_PROJECT_CODE) //
                .build() //
        );
    }
}
