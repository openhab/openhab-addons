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
package org.openhab.binding.tapocontrol.internal.devices.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;

/**
 * What a Tapo device needs from its parent bridge, which is either a {@code tapocontrol:bridge} or a shared
 * {@code tplinkbridge:account}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public interface TapoDeviceBridge {

    TapoCredentials getCredentials();

    HttpClient getHttpClient();

    /**
     * @return the Tapo bridge, which also runs device discovery, or {@code null} for a {@code tplinkbridge:account}
     */
    @Nullable
    TapoBridgeHandler getTapoBridge();
}
