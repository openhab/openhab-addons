/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.smsmodem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * DeliveryStatus enum for delivery report status
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public enum DeliveryStatus {
    UNKNOWN,
    QUEUED,
    SENT,
    PENDING,
    DELIVERED,
    EXPIRED,
    FAILED
}
