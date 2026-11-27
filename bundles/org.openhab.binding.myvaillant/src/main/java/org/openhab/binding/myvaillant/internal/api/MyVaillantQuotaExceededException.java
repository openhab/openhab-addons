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
package org.openhab.binding.myvaillant.internal.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Vaillant limits the call volume per account. Once exceeded, every request fails until the quota is replenished, so
 * no requests are sent until then.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class MyVaillantQuotaExceededException extends MyVaillantException {

    private static final long serialVersionUID = 1L;

    private final Instant blockedUntil;

    public MyVaillantQuotaExceededException(Instant blockedUntil) {
        super("API call volume quota exceeded until " + blockedUntil);
        this.blockedUntil = blockedUntil;
    }

    public Instant getBlockedUntil() {
        return blockedUntil;
    }
}
