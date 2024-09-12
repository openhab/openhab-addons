/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.network.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SpeedTestConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SpeedTestConfiguration {
    public int refreshInterval = 20;
    public int initialDelay = 5;
    public int uploadSize = 1000000;
    public int maxTimeout = 3;
    private @Nullable String url;
    private @Nullable String fileName;

    public @Nullable String getUploadURL() {
        String localUrl = url;
        if (localUrl != null) {
            localUrl += localUrl.endsWith("/") ? "" : "/";
        }
        return localUrl;
    }

    public @Nullable String getDownloadURL() {
        String result = getUploadURL();
        if (result != null && fileName != null) {
            result += fileName;
        }
        return result;
    }
}
