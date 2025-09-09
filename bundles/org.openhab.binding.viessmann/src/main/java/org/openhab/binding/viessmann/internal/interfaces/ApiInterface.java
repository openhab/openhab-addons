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
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link ApiInterface} defines the contract for interactions with the Viessmann API.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Manage installation and gateway identifiers</li>
 * <li>Update bridge status information</li>
 * <li>Provide access to the Thing UID</li>
 * <li>Handle API call limits</li>
 * </ul>
 *
 * <p>
 * <b>Null-handling:</b> All methods explicitly document how {@code null} inputs
 * are processed (ignored, set to default, or only partially update values).
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public interface ApiInterface {

    /**
     * Sets the installation ID and gateway ID for the current API context.
     * <p>
     * If either parameter is {@code null}, that value will be ignored and not updated.
     *
     * @param newInstallation the installation ID to set, or {@code null} if not updated
     * @param newGateway the gateway ID to set, or {@code null} if not updated
     */
    void setInstallationGatewayId(@Nullable String newInstallation, @Nullable String newGateway);

    /**
     * Updates the current bridge status.
     * <p>
     * If {@code status} is {@code null}, the call is ignored.
     *
     * @param status the new {@link ThingStatus} for the bridge, or {@code null} if unchanged
     */
    void updateBridgeStatus(@Nullable ThingStatus status);

    /**
     * Updates the current bridge status with additional detail and message.
     * <p>
     * If any of the parameters are {@code null}, that part of the update is skipped.
     *
     * @param status the new {@link ThingStatus}, or {@code null} if unchanged
     * @param statusDetail the new {@link ThingStatusDetail}, or {@code null} if unchanged
     * @param statusMessage the status message, or {@code null} if not provided
     */
    void updateBridgeStatusExtended(@Nullable ThingStatus status, @Nullable ThingStatusDetail statusDetail,
            @Nullable String statusMessage);

    /**
     * Returns the UID of the related Thing as a string.
     *
     * @return the Thing UID string, never {@code null}
     */
    String getThingUIDasString();

    /**
     * Waits until the API call limit is reset, if necessary.
     * <p>
     * If {@code limitReset} is {@code null}, no waiting is performed.
     *
     * @param limitReset the timestamp (epoch milliseconds) when the API limit resets,
     *            or {@code null} if no waiting is required
     */
    void waitForApiCallLimitReset(@Nullable Long limitReset);
}
