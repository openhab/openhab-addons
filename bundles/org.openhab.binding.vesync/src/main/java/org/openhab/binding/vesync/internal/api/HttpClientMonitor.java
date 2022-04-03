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
package org.openhab.binding.vesync.internal.api;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;

/**
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class HttpClientMonitor {

    CopyOnWriteArrayList<IHttpClientWatcher> httpClientWatchers = new CopyOnWriteArrayList<>();

    @Nullable
    HttpClient activeHttpClientRef = null;

    public interface IHttpClientWatcher {
        void handleNewHttpClient(final @Nullable HttpClient newClientRef);
    }

    public void addWatcher(@NotNull IHttpClientWatcher watcher) {
        httpClientWatchers.add(watcher);
        watcher.handleNewHttpClient(activeHttpClientRef);
    }

    public void removeWatcher(@NotNull IHttpClientWatcher watcher) {
        httpClientWatchers.remove(watcher);
        watcher.handleNewHttpClient(null);
    }

    public void setNewHttpClient(@Nullable HttpClient newClient) {
        activeHttpClientRef = newClient;
        httpClientWatchers.forEach(client -> client.handleNewHttpClient(activeHttpClientRef));
    }
}
