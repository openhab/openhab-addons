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
package org.openhab.binding.millheat.internal.model;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A mode together with the period it applies to. The cloud API supplies the period only for
 * overrides and vacations, so both instants may be absent.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Use instants from the cloud API
 */
@NonNullByDefault
public record Mode(ModeType mode, @Nullable Instant start, @Nullable Instant end) {

    public static Mode of(final ModeType mode) {
        return new Mode(mode, null, null);
    }
}
