/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class Configuration {
    public static final String VERSION = "10.11.3";

    private static final AtomicReference<ApiClient> defaultApiClient = new AtomicReference<>();
    private static volatile Supplier<ApiClient> apiClientFactory = ApiClient::new;

    /**
     * Get the default API client, which would be used when creating API instances without providing an API client.
     *
     * @return Default API client
     */
    public static ApiClient getDefaultApiClient() {
        ApiClient client = defaultApiClient.get();
        if (client == null) {
            client = defaultApiClient.updateAndGet(val -> {
                if (val != null) { // changed by another thread
                    return val;
                }
                return apiClientFactory.get();
            });
        }
        return client;
    }

    /**
     * Set the default API client, which would be used when creating API instances without providing an API client.
     *
     * @param apiClient API client
     */
    public static void setDefaultApiClient(ApiClient apiClient) {
        defaultApiClient.set(apiClient);
    }

    /**
     * set the callback used to create new ApiClient objects
     */
    public static void setApiClientFactory(Supplier<ApiClient> factory) {
        apiClientFactory = Objects.requireNonNull(factory);
    }

    private Configuration() {
    }
}
