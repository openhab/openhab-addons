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
package org.openhab.binding.meteoalerte.internal.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Meta {
    public @Nullable String snapshotId;
    public @Nullable ZonedDateTime productDatetime; // date/heure de diffusion du produit
    public @Nullable ZonedDateTime generationTimestamp; // date/heure de début de validité du produit
}
