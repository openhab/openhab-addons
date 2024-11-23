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
package org.openhab.binding.sleepiq.internal.api.impl.typeadapters;

import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Type adapter for jsr310 {@link LocalDate} class.
 *
 * @author Christophe Bornet - Initial contribution
 */
@NonNullByDefault
public class LocalDateTypeAdapter extends TemporalTypeAdapter<LocalDate> {

    public LocalDateTypeAdapter() {
        super(LocalDate::parse);
    }
}
