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
package org.openhab.binding.linktap.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceMetaDataUpdatedHandler} enables call-backs for when the device meta-data is updated from a bridge.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface DeviceMetaDataUpdatedHandler {
    /**
     * Any registered metadata handlers, will have this
     * invoked after new configuration data has been retrieved from the GW.
     *
     * An example use is for the discovery service to refresh its data based on received
     * new configuration data of devices attached to a GW.
     */
    void handleMetadataRetrieved(LinkTapBridgeHandler handler);
}
