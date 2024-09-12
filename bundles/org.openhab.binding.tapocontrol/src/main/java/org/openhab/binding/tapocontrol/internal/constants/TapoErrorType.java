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
package org.openhab.binding.tapocontrol.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoErrorType} enum lists known errortypes can be received or thrown by binding
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public enum TapoErrorType {
    COMMUNICATION_ERROR, // communication error
    COMMUNICATION_RETRY, // communication error - retry to connect immediately
    CONFIGURATION_ERROR, // configuration error
    GENERAL, // general error (e.g. known api err)
    UNKNOWN // unknown error
}
