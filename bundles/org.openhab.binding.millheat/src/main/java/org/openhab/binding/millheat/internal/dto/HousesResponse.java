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
package org.openhab.binding.millheat.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Response of {@code GET /houses}. Houses shared with the account are reported separately from
 * those the account owns, but both are usable.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record HousesResponse(@Nullable List<HouseDTO> ownHouses, @Nullable List<HouseDTO> sharedHouses) {
}
