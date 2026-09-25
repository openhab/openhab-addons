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
package org.openhab.binding.hasslink.internal.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration class for {@link org.openhab.binding.hasslink.internal.handler.HassLinkDeviceHandler}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HassLinkDeviceConfiguration {

    public @Nullable String deviceId;
    public List<String> entityIds = new ArrayList<>();

    // Filtering Parameters
    public List<String> includedDomains = List.of();
    public List<String> excludedDomains = List.of();
    public List<String> includedLabels = List.of();
    public List<String> excludedLabels = List.of();
    public List<String> includedEntities = List.of();
    public List<String> excludedEntities = List.of();
}
