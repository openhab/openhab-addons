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
package org.openhab.binding.meteofrance.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;

/**
 * {@link MeteoFranceChildHandler} is a common interface for Things having a
 * {@link MeteoFranceBridgeHandler} Bridge
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public interface MeteoFranceChildHandler {
    default Optional<MeteoFranceBridgeHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            if (bridge.getHandler() instanceof MeteoFranceBridgeHandler maHandler) {
                return Optional.of(maHandler);
            }
        }
        return Optional.empty();
    }

    @Nullable
    Bridge getBridge();
}
