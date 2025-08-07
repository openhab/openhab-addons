/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.api.ViessmannCommunicationException;
import org.openhab.binding.viessmann.internal.handler.DeviceHandler;

/**
 * The {@link BridgeInterface} is responsible for handling Bridges
 *
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public interface BridgeInterface {

    void setConfigInstallationGatewayIdToDevice(@Nullable DeviceHandler handler);

    boolean setData(@Nullable String url, @Nullable String json) throws ViessmannCommunicationException;

    void updateFeaturesOfDevice(@Nullable DeviceHandler handler);
}
