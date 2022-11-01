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
package org.openhab.binding.hdpowerview.internal.dto.responses;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.dto.ShadeData;

/**
 * State of all Shades, as returned by an HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class Shades {
    public @Nullable List<ShadeData> shadeData;
    public @Nullable List<Integer> shadeIds;
}
