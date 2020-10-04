/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.http;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.HttpUtil;

/**
 * The {@link HttpUtil_VOC} is basically a clone of {@link HttpUtil}, (from opehnhab-core)
 * though it allows external entities to override its private static copy of HttpClientFactory.
 * 
 * @implNote This class was created for the sole purpose of backporting a 'User-Agent' fix (#8554)
 *           to 2.5.x branch, without the need of refactoring VOC binding or openhab-core.
 *           ##This should not be merged into OH3##
 *
 * @author Mateusz Bronk - Initial contribution for the purpose of backporting #8554 fix.
 */
@NonNullByDefault
public class HttpUtil_VOC extends HttpUtil {
    /**
     * @implNote Instantiating this class is not really required for any purpose, as all the fields
     *           are static. It is there just to access the protected 'super.setHttpClientFactory()'
     *           and to avoid reflection or forcing clients of this class to use a stray 'new'
     */
    private static final HttpUtil_VOC instance = new HttpUtil_VOC();

    private HttpUtil_VOC() {
    }

    /**
     * Overrides HttpClientFactory used by {@link HttpUtil_VOC} static methods
     * 
     * @param httpClientFactory The new HttpClientFactory to use
     */
    public static void SetHttpClientFactory(@Nullable final HttpClientFactory httpClientFactory) {
        instance.setHttpClientFactory(httpClientFactory);
    }
}
