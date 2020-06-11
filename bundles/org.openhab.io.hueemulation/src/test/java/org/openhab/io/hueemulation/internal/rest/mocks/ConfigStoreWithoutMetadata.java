/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.rest.mocks;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Usually the metadata registry is used to map item UIDs to integer hue IDs.
 * For tests we do not need this extra complexity and just map the item UID to the hue ID.
 *
 * @author David Graeff - Initial contribution
 */
public class ConfigStoreWithoutMetadata extends ConfigStore {

    public ConfigStoreWithoutMetadata(NetworkAddressService networkAddressService, ConfigurationAdmin configAdmin,
            ScheduledExecutorService scheduler) {
        super(networkAddressService, configAdmin, null, scheduler);
    }

    @Override
    protected void determineHighestAssignedHueID() {
    }

    @Override
    public @NonNull String mapItemUIDtoHueID(@Nullable Item item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        return item.getUID();
    }
}
