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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.HomeStatusError;

/**
 * The {@link NAError} class is the base class for errors returned by the Netatmo API
 * regarding a given thing.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NAError extends NAObject {
    private HomeStatusError code = HomeStatusError.UNKNOWN;

    public HomeStatusError getCode() {
        return code;
    }
}
