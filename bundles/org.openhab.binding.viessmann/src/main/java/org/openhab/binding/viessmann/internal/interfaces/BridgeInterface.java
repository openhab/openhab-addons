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
 * The {@link BridgeInterface} defines the contract for communication and
 * interaction between the Viessmann bridge, Viessmann gateway and connected devices.
 *
 * It is responsible for:
 * <ul>
 * <li>Assigning configuration details to devices</li>
 * <li>Sending data to the Viessmann API</li>
 * <li>Updating device features from the bridge</li>
 * </ul>
 *
 * <p>
 * <b>Null-handling:</b> All methods explicitly document how {@code null} inputs
 * are processed (ignored, cause an exception, or return a default value).
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public interface BridgeInterface {

    /**
     * Sets the installation gateway ID from the configuration into the given device handler.
     *
     * @param handler the {@link DeviceHandler} instance that should receive the gateway ID,
     *            or {@code null} if no device is currently assigned
     */
    void setConfigInstallationGatewayIdToDevice(@Nullable DeviceHandler handler);

    /**
     * Sends data to the Viessmann API.
     *
     * @param url the endpoint URL to which the data should be sent, may be {@code null}
     * @param json the JSON payload containing the data, may be {@code null}
     * @return {@code true} if the data was successfully sent, otherwise {@code false}
     * @throws ViessmannCommunicationException if a communication error occurs with the API
     */
    boolean setData(@Nullable String url, @Nullable String json) throws ViessmannCommunicationException;

    /**
     * Updates the features of a given device by requesting data from the bridge.
     *
     * @param handler the {@link DeviceHandler} whose features should be updated,
     *            or {@code null} if no device is currently assigned
     */
    void updateFeaturesOfDevice(@Nullable DeviceHandler handler);
}
