/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.util;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Device;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceIdentLabel;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Ident;

/**
 * Utility class for creating common mocks.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class MockUtil {
    private MockUtil() {
    }

    public static Device mockDevice(String fabNumber) {
        DeviceIdentLabel deviceIdentLabel = mock(DeviceIdentLabel.class);
        when(deviceIdentLabel.getFabNumber()).thenReturn(Optional.of(fabNumber));

        Ident ident = mock(Ident.class);
        when(ident.getDeviceIdentLabel()).thenReturn(Optional.of(deviceIdentLabel));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));

        return device;
    }

    public static <T> T requireNonNull(@Nullable T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object must not be null");
        }
        return obj;
    }

    /**
     * Creates a mock for {@link HttpClient} circumventing the problem that {@link HttpClient#start()} is {@code final}
     * and {@link HttpClient#doStart()} {@code protected} and unaccessible when mocking with Mockito.
     */
    public static HttpClient mockHttpClient() {
        return new HttpClient() {
            @Override
            protected void doStart() throws Exception {
            }
        };
    }

    /**
     * Creates a mock for {@link HttpClient} circumventing the problem that {@link HttpClient#start()} is {@code final}
     * and {@link HttpClient#doStart()} {@code protected} and unaccessible when mocking with Mockito.
     *
     * @param newRequestUri {@code uri} parameter of {@link HttpClient#newRequest(String)} to mock.
     * @param newRequestReturnValue Return value of {@link HttpClient#newRequest(String)} to mock.
     */
    public static HttpClient mockHttpClient(String newRequestUri, Request newRequestReturnValue) {
        return new HttpClient() {
            @Override
            protected void doStart() throws Exception {
            }

            @Override
            public Request newRequest(@Nullable String uri) {
                if (newRequestUri.equals(uri)) {
                    return newRequestReturnValue;
                } else {
                    fail();
                    throw new IllegalStateException();
                }
            }
        };
    }
}
