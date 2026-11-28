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
package org.openhab.binding.atv.internal.client.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Apps;
import org.openhab.binding.atv.internal.client.dto.App;

/**
 * Relay implementation for app handling.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AppsRelay extends BaseRelay<Apps> implements Apps {

    /**
     * Creates a new relay apps instance.
     *
     * @param guard device guard blocking calls after close
     */
    public AppsRelay(Guard guard) {
        super(new Relayer<>(Apps.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
    }

    @Override
    public CompletableFuture<List<App>> appList() {
        guard.requireNotBlocked("appList");
        return relayAsync(Capability.APPS_APP_LIST, Apps::appList);
    }

    @Override
    public CompletableFuture<Void> launchApp(String bundleIdOrUrl) {
        guard.requireNotBlocked("launchApp");
        return relayAsync(Capability.APPS_LAUNCH_APP, apps -> apps.launchApp(bundleIdOrUrl));
    }
}
