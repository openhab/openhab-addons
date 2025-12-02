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
package org.openhab.binding.ferroamp.internal.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FerroAmpUpdateListener} interface is used to listen for updates from the Ferroamp system.
 *
 * @author Leo Siepel - Initial contribution
 *
 */

@NonNullByDefault
public interface FerroAmpUpdateListener {

    /**
     * This method is called when the Ferroamp data is updated.
     *
     * @param keyValueMap a map containing the key-value pairs of the data
     * @param type the type of data being updated (e.g., EHUB, SSO, ESM, ESO)
     */
    void onFerroAmpUpdateListener(DataType type, Map<String, @Nullable String> keyValueMap);
}