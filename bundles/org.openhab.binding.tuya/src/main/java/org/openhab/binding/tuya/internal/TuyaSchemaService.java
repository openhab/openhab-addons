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
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PRODUCT_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.DEVICE_THING_TYPES;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link TuyaSchemaService} reloads device schemas from the Tuya cloud.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
@Component(service = TuyaSchemaService.class)
public class TuyaSchemaService {
    private static final int SCHEMA_REQUEST_TIMEOUT = 30; // Seconds

    private final ThingRegistry thingRegistry;
    private final ThingManager thingManager;
    private final TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider;
    private final TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final Gson gson = new Gson();

    @Activate
    public TuyaSchemaService(final @Reference ThingRegistry thingRegistry, final @Reference ThingManager thingManager,
            final @Reference TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider,
            final @Reference TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        this.thingRegistry = thingRegistry;
        this.thingManager = thingManager;
        this.dynamicCommandDescriptionProvider = dynamicCommandDescriptionProvider;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    /**
     * Retrieves the specification of a device from the cloud and stores it as the schema of its product. Things
     * using the product have to be re-initialized afterwards, see {@link #reinitializeThings(String)}.
     *
     * @param productId the product whose schema is replaced
     * @param deviceId the device whose specification is retrieved
     * @return the stored data points, completed exceptionally with a {@link SchemaReloadException} if the schema
     *         cannot be reloaded, in which case the stored schema is left unchanged
     */
    public CompletableFuture<List<SchemaDp>> reloadSchema(String productId, String deviceId) {
        if (productId.isBlank() || deviceId.isBlank()) {
            return CompletableFuture.failedFuture(new SchemaReloadException("product ID and device ID are required"));
        }
        if (TuyaSchemaDB.isBuiltIn(productId)) {
            return CompletableFuture.failedFuture(new SchemaReloadException("the schema of product '" + productId
                    + "' is built into the binding and cannot be reloaded from the cloud"));
        }

        List<ProjectHandler> projectHandlers = connectedProjectHandlers();
        if (projectHandlers.isEmpty()) {
            return CompletableFuture.failedFuture(new SchemaReloadException(
                    "no Tuya cloud project is connected, an ONLINE project thing is required"));
        }

        return requestSchema(deviceId, projectHandlers, 0).thenApply(schemaDps -> {
            TuyaSchemaDB.put(productId, schemaDps);
            return schemaDps;
        });
    }

    /**
     * Device things are not linked to a project, so every connected project is tried until one knows the device.
     */
    private CompletableFuture<List<SchemaDp>> requestSchema(String deviceId, List<ProjectHandler> projectHandlers,
            int index) {
        ProjectHandler projectHandler = projectHandlers.get(index);
        return projectHandler.getDeviceSchema(deviceId).orTimeout(SCHEMA_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .thenApply(schema -> {
                    List<SchemaDp> schemaDps = SchemaDp.fromRemoteSchema(gson, schema);
                    if (schemaDps.isEmpty()) {
                        throw new CompletionException(new SchemaReloadException("no data points"));
                    }
                    return schemaDps;
                }).exceptionallyCompose(e -> {
                    if (index + 1 < projectHandlers.size()) {
                        return requestSchema(deviceId, projectHandlers, index + 1);
                    }
                    return CompletableFuture.failedFuture(new SchemaReloadException(
                            "retrieving the schema of device '" + deviceId + "' from project '"
                                    + projectHandler.getThing().getUID() + "' failed: " + failureReason(e),
                            e));
                });
    }

    private static String failureReason(Throwable e) {
        Throwable cause = e instanceof CompletionException ? Objects.requireNonNullElse(e.getCause(), e) : e;
        if (cause instanceof TimeoutException) {
            return "timeout";
        }
        String message = cause.getMessage();
        return message != null ? message : cause.getClass().getSimpleName();
    }

    /**
     * Re-initializes all enabled things of a product so they pick up the current schema. Learned command and
     * state options of their channels are discarded while the things are disabled.
     *
     * @param productId the product whose things are re-initialized
     * @return the UIDs of the re-initialized things
     */
    public List<ThingUID> reinitializeThings(String productId) {
        List<ThingUID> reinitialized = new ArrayList<>();
        for (Thing thing : thingRegistry.getAll()) {
            if (!DEVICE_THING_TYPES.contains(thing.getThingTypeUID())
                    || !productId.equals(thing.getConfiguration().get(CONFIG_PRODUCT_ID))) {
                continue;
            }
            ThingUID thingUID = thing.getUID();
            boolean enabled = thingManager.isEnabled(thingUID);
            if (enabled) {
                thingManager.setEnabled(thingUID, false);
            }
            // A running handler keeps learning options, so they are only removed once it is disposed.
            dynamicCommandDescriptionProvider.removeCommandDescriptions(thingUID);
            dynamicStateDescriptionProvider.removeStateDescriptions(thingUID);
            if (enabled) {
                thingManager.setEnabled(thingUID, true);
                reinitialized.add(thingUID);
            }
        }
        return reinitialized;
    }

    private List<ProjectHandler> connectedProjectHandlers() {
        List<ProjectHandler> projectHandlers = new ArrayList<>();
        for (Thing thing : thingRegistry.getAll()) {
            if (thing.getHandler() instanceof ProjectHandler projectHandler && projectHandler.getApi().isConnected()) {
                projectHandlers.add(projectHandler);
            }
        }
        return projectHandlers;
    }
}
