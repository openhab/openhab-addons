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
package org.openhab.binding.modbus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The immutable outcome of submitting a poller2 write request.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public enum Poller2WriteResult {
    ACCEPTED,
    HANDLER_UNAVAILABLE,
    UNKNOWN_CHANNEL,
    TYPE_MISMATCH,
    INVALID_COMMAND,
    NOT_READY
}
