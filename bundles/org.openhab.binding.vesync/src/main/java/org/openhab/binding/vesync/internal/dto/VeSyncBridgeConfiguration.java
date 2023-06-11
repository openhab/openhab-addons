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
package org.openhab.binding.vesync.internal.dto;

import static org.openhab.binding.vesync.internal.VeSyncConstants.DEFAULT_POLL_INTERVAL_AIR_FILTERS_DEVICES;
import static org.openhab.binding.vesync.internal.VeSyncConstants.DEFAULT_REFRESH_INTERVAL_DISCOVERED_DEVICES;

/**
 * The {@link VeSyncBridgeConfiguration} is a container for all the bridge configuration.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncBridgeConfiguration {

    public String username;
    public String password;
    public long airPurifierPollInterval = DEFAULT_POLL_INTERVAL_AIR_FILTERS_DEVICES;
    public boolean backgroundDeviceDiscovery;
    public long refreshBackgroundDeviceDiscovery = DEFAULT_REFRESH_INTERVAL_DISCOVERED_DEVICES;
}
