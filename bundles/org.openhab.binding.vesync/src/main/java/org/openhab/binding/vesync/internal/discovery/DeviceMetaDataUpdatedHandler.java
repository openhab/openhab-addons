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
package org.openhab.binding.vesync.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.handlers.VeSyncBridgeHandler;

/**
 * The {@link DeviceMetaDataUpdatedHandler} enables call-backs for when the device meta-data is updated from a bridge.
 * (VeSync Server Account).
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface DeviceMetaDataUpdatedHandler {
    void handleMetadataRetrieved(VeSyncBridgeHandler handler);
}
