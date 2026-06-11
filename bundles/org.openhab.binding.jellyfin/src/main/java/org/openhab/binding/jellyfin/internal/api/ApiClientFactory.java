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
package org.openhab.binding.jellyfin.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Factory for creating and configuring {@link ApiClientWrapper} instances for Jellyfin server interactions.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.SINGLETON, configurationPid = "api.jellyfin", service = ApiClientFactory.class)
public class ApiClientFactory {
    @Activate
    public ApiClientFactory() {
    }

    /**
     * Creates and configures a new instance of {@link ApiClientWrapper}.
     * <p>
     * The client is initialized and can be further configured with an HTTP client factory and a base URL.
     * Additional configuration steps can be added as needed.
     *
     * @return a newly created and partially configured {@link ApiClientWrapper} instance
     */
    public ApiClientWrapper createApiClient() {
        ApiClientWrapper client = new ApiClientWrapper();
        // Configure the client with HTTP client factory and base URL
        // Add any additional configuration here
        return client;
    }
}
