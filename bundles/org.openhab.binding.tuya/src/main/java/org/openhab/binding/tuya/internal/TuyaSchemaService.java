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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link TuyaSchemaService} reloads device schemas from the Tuya cloud. It is shared by the console command and
 * the {@code reloadSchema} configuration parameter of device things.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
@Component(service = TuyaSchemaService.class)
public class TuyaSchemaService {
    private static final long SCHEMA_REQUEST_TIMEOUT_SECONDS = 30;

    private final ThingRegistry thingRegistry;
    private final TuyaChannelTypeProvider channelTypeProvider;
    private final Gson gson = new Gson();

    @Activate
    public TuyaSchemaService(final @Reference ThingRegistry thingRegistry,
            final @Reference TuyaChannelTypeProvider channelTypeProvider) {
        this.thingRegistry = thingRegistry;
        this.channelTypeProvider = channelTypeProvider;
    }

    /**
     * Retrieves the specification of a device from the cloud, stores it as the schema of its product and discards
     * the channel types generated from the previous schema. Things using the product have to be re-initialized
     * afterwards to pick up the new schema.
     *
     * @param productId the product whose schema is replaced
     * @param deviceId the device whose specification is retrieved
     * @return the stored datapoints
     * @throws SchemaReloadException if the schema cannot be reloaded, the stored schema is left unchanged
     */
    public List<SchemaDp> reloadSchema(String productId, String deviceId) throws SchemaReloadException {
        if (productId.isBlank() || deviceId.isBlank()) {
            throw new SchemaReloadException("product ID and device ID are required");
        }
        if (TuyaSchemaDB.isBuiltIn(productId)) {
            throw new SchemaReloadException("the schema of product '" + productId
                    + "' is built into the binding and cannot be reloaded from the cloud");
        }

        ProjectHandler projectHandler = findConnectedProjectHandler();
        if (projectHandler == null) {
            throw new SchemaReloadException("no Tuya cloud project is connected, an ONLINE project thing is required");
        }

        List<SchemaDp> schemaDps;
        try {
            DeviceSchema schema = projectHandler.getDeviceSchema(deviceId).get(SCHEMA_REQUEST_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            schemaDps = SchemaDp.fromRemoteSchema(gson, schema);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new SchemaReloadException("retrieving the schema of device '" + deviceId + "' failed: "
                    + (cause != null ? cause.getMessage() : e.getMessage()), e);
        } catch (TimeoutException e) {
            throw new SchemaReloadException("retrieving the schema of device '" + deviceId + "' timed out", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SchemaReloadException("retrieving the schema of device '" + deviceId + "' was interrupted", e);
        }

        if (schemaDps.isEmpty()) {
            throw new SchemaReloadException("the cloud reported no datapoints for device '" + deviceId + "'");
        }

        TuyaSchemaDB.put(productId, schemaDps);
        channelTypeProvider.removeChannelTypes(productId);
        return schemaDps;
    }

    private @Nullable ProjectHandler findConnectedProjectHandler() {
        for (Thing thing : thingRegistry.getAll()) {
            if (thing.getHandler() instanceof ProjectHandler projectHandler && projectHandler.getApi().isConnected()) {
                return projectHandler;
            }
        }
        return null;
    }
}
