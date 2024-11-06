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
package org.openhab.binding.electroluxappliance.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TokenUpdateListener} callback interface for notifying about token updates
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public interface TokenUpdateListener {
    /**
     * Called when the access token and refresh token are updated.
     *
     * @param newAccessToken the new access token
     * @param newRefreshToken the new refresh token
     */
    void onTokenUpdated(String newRefreshToken);
}
