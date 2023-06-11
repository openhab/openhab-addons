/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LocationEx} is the common interface for dto holding an extra location data
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public interface LocationEx extends Location {
    Optional<String> getCountry();

    Optional<String> getTimezone();
}
