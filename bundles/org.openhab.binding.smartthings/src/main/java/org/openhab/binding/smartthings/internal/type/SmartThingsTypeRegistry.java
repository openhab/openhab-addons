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
package org.openhab.binding.smartthings.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.handler.SmartThingsCloudBridgeHandler;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface SmartThingsTypeRegistry {

    /**
     * Initializes the type generator.
     */
    void initialize();

    void register(String deviceType, SmartThingsDevice device);

    void registerCapability(SmartThingsCapability capa);

    @Nullable
    SmartThingsCapability getCapability(String capaKey);

    @Nullable
    public SmartThingsChannelTypeProvider getSmartThingsChannelTypeProvider();

    public void setCloudBridgeHandler(SmartThingsCloudBridgeHandler bridgeHandler);
}
