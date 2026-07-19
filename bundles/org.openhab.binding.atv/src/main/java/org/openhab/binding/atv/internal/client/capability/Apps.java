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
package org.openhab.binding.atv.internal.client.capability;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.App;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for app handling.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Apps {

    /**
     * Fetches a list of apps that can be launched.
     *
     * @return future completing with launchable apps
     */
    default CompletableFuture<List<App>> appList() {
        return CompletableFuture.failedFuture(new NotSupportedError("appList is not supported"));
    }

    /**
     * Launches an app based on bundle ID or URL.
     *
     * @param bundleIdOrUrl bundle identifier or URL of app to launch
     * @return future completing when app has been launched
     */
    default CompletableFuture<Void> launchApp(String bundleIdOrUrl) {
        return CompletableFuture.failedFuture(new NotSupportedError("launchApp is not supported"));
    }
}
