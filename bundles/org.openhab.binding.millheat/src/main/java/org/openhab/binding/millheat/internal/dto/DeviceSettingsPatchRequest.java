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

import java.util.Map;

/**
 * Body of {@code PATCH /devices/&#123;deviceId&#125;/settings}. The contents of {@code settings}
 * replace {@code state.desired} in the device's AWS IoT Thing shadow, so only the keys being
 * changed are sent. {@code deviceType} must name the device family, for example {@code Heaters}.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record DeviceSettingsPatchRequest(String deviceType, boolean enabled, Map<String, Object> settings) {
}
