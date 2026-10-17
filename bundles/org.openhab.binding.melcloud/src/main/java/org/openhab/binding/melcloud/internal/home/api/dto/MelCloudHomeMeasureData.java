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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One entry of a {@code telemetry/energy} response's {@code measureData} array.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeMeasureData {

    public String deviceId = "";
    public String type = "";
    public List<MelCloudHomeMeasureValue> values = List.of();
}
