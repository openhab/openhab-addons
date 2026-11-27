/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.myvaillant.internal.discovery;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.handler.AccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Discovers the systems (homes) of an account. The homes are cached by the API client, so scans rarely cost a request.
 *
 * @author Marius Korte - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AccountDiscoveryService.class)
@NonNullByDefault
public class AccountDiscoveryService extends AbstractThingHandlerDiscoveryService<AccountHandler> {

    private static final int SCAN_TIMEOUT_SECONDS = 30;
    private static final long BACKGROUND_DELAY_SECONDS = 60;
    private static final long BACKGROUND_INTERVAL_SECONDS = 30 * 60;

    private final Logger logger = LoggerFactory.getLogger(AccountDiscoveryService.class);
    private @Nullable ScheduledFuture<?> backgroundFuture;

    public AccountDiscoveryService() {
        super(AccountHandler.class, Set.of(THING_TYPE_SYSTEM), SCAN_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startScan() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        try {
            for (JsonElement element : thingHandler.getHomes()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject home = element.getAsJsonObject();
                String systemId = JsonUtil.getString(home, "system_id");
                if (systemId == null || systemId.isBlank()) {
                    continue;
                }
                String homeName = JsonUtil.getString(home, "home_name");
                String nomenclature = JsonUtil.getString(home, "nomenclature");
                String label = "myVAILLANT " + (homeName == null || homeName.isBlank() ? systemId : homeName)
                        + (nomenclature == null ? "" : " (" + nomenclature + ")");
                ThingUID uid = new ThingUID(THING_TYPE_SYSTEM, bridgeUID, toThingId(systemId));
                thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(label)
                        .withProperty(CONFIG_SYSTEM_ID, systemId).withRepresentationProperty(CONFIG_SYSTEM_ID).build());
            }
        } catch (MyVaillantException e) {
            if (!thingHandler.handleAccountFailure(e)) {
                logger.debug("Discovery of myVAILLANT systems failed: {}", e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static String toThingId(String id) {
        return id.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        backgroundFuture = scheduler.scheduleWithFixedDelay(this::startScan, BACKGROUND_DELAY_SECONDS,
                BACKGROUND_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> future = backgroundFuture;
        if (future != null) {
            future.cancel(true);
            backgroundFuture = null;
        }
    }
}
