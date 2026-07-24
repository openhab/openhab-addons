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
package org.openhab.binding.melcloud.internal.home.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A simple {@code {"enabled": bool}} object, used for an Air-to-Water unit's top-level {@code holidayMode} and
 * {@code frostProtection} fields (see ADR-003) — unlike most unit state, these are not part of the {@code settings}
 * array.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeToggleState {

    public boolean enabled;
}
