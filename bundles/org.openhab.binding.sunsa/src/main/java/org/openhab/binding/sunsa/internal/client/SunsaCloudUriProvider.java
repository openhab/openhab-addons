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

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides the uris to communicate with the sunsa cloud service.
 * 
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaCloudUriProvider {
    public static interface BaseUriProvider extends Supplier<String> {
    }

    private static final String DEFAULT_URI_BASE = "https://sunsahomes.com/api/public/";
    private static final String PATH_DEVICES = "{userId}/devices";
    private static final String PATH_DEVICE = "{userId}/devices/{idDevice}";

    private final String baseUri;

    /**
     * @param baseUri If provided, this uri will be used as the base for all other uris. The default
     *            {@value #DEFAULT_URI_BASE} is used otherwise.
     */
    public SunsaCloudUriProvider(BaseUriProvider baseUriProvider) {
        this.baseUri = Optional.ofNullable(baseUriProvider.get()).filter(it -> !it.isEmpty()).orElse(DEFAULT_URI_BASE);
    }

    public String getDevicesUri() {
        return baseUri + PATH_DEVICES;
    }

    public String getDeviceUri() {
        return baseUri + PATH_DEVICE;
    }
}
