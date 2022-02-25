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
package org.openhab.binding.sunsa.internal.client;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sunsa.internal.domain.Device;
import org.openhab.binding.sunsa.internal.domain.GetDevicesResponse;
import org.openhab.binding.sunsa.internal.domain.ImmutableDevice;
import org.openhab.binding.sunsa.internal.domain.PutDeviceResponse;
import org.openhab.binding.sunsa.internal.util.PositionAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cloud based implementation {@link SunsaService}.
 * 
 * @see <a href="https://app.swaggerhub.com/apis/Sunsa/Sunsa/1.0.4">Sunsa API</a>
 * 
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaCloudService implements SunsaService {
    public static interface UserIdProvider extends Supplier<String> {
    }

    public static interface ApiKeyProvider extends Supplier<String> {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SunsaCloudService.class);

    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_DEVICE_ID = "idDevice";
    private static final String ATTR_API_KEY = "publicApiKey";

    private final Client restClient;
    private final UserIdProvider userIdProvider;
    private final ApiKeyProvider apiKeyProvider;
    private final SunsaCloudUriProvider cloudUriProvider;

    public SunsaCloudService(final ClientBuilder clientBuilder, final UserIdProvider userIdProvider,
            final ApiKeyProvider apiKeyProvider, final SunsaCloudUriProvider cloudUriProvider) {
        this.userIdProvider = requireNonNull(userIdProvider);
        this.apiKeyProvider = requireNonNull(apiKeyProvider);
        this.cloudUriProvider = requireNonNull(cloudUriProvider);
        this.restClient = clientBuilder.register(new ClientRequestFilter() {
            @Override
            public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
                LOGGER.debug("request: {uri:{}, entity:{}}", requestContext.getUri(), requestContext.getEntity());
            }
        }).build();
    }

    @Override
    public List<Device> getDevices() throws SunsaException {
        try (final Response response = getAuthenticatedTarget(cloudUriProvider.getDevicesUri())
                .resolveTemplate(ATTR_USER_ID, userIdProvider.get()).request(MediaType.APPLICATION_JSON).get()) {
            throwExceptionIfInvalidResponse(response);
            return response.readEntity(GetDevicesResponse.class).getDevices();
        }
    }

    @Override
    public Device updateDevice(Device device) throws SunsaException {
        final Invocation invocation = getAuthenticatedTarget(cloudUriProvider.getDeviceUri())
                .resolveTemplates(Map.of(ATTR_USER_ID, userIdProvider.get(), ATTR_DEVICE_ID, device.getId()))
                .request(MediaType.APPLICATION_JSON).buildPut(Entity.json(device));
        try (final Response response = invocation.invoke()) {
            throwExceptionIfInvalidResponse(response);
            return response.readEntity(PutDeviceResponse.class).getDevice();
        }
    }

    @Override
    public Device getDevice(String id) throws SunsaException {
        // NOTE: Sunsa does not support GET requests for a device, only PUT...
        // So we fetch the whole list to get device info...
        return getDevices().stream().filter(device -> device.getId().equals(id)).findFirst()
                .orElseThrow(() -> new UnknownDeviceException(404, "Device not found"));
    }

    @Override
    public int setDevicePosition(String deviceId, int rawPosition) {
        final int clampedPosition = PositionAdapters.clampRawPosition(rawPosition);
        final Device updatedDevice = ImmutableDevice.builder().id(deviceId).rawPosition(clampedPosition).build();
        return updateDevice(updatedDevice).getRawPosition();
    }

    private WebTarget getAuthenticatedTarget(final String uri) {
        return restClient.target(uri).queryParam(ATTR_API_KEY, apiKeyProvider.get());
    }

    private void throwExceptionIfInvalidResponse(final Response response) {
        final StatusType statusType = response.getStatusInfo();
        if (statusType.getFamily() != Family.SUCCESSFUL) {
            if (HttpStatus.isClientError(response.getStatus())) {
                throw new ClientException(response.getStatus(), statusType.getReasonPhrase());
            } else {
                throw new ServiceException(response.getStatus(), statusType.getReasonPhrase());
            }
        }
    }
}
