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
package org.openhab.binding.nuvo.internal.configuration;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NuvoBindingConfiguration} is responsible for holding configuration of the binding itself.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class NuvoBindingConfiguration {
    public final String imageHeight;
    public final String imageWidth;

    public NuvoBindingConfiguration(Map<String, Object> config) {
        this.imageHeight = (String) config.getOrDefault("imageHeight", "150");
        this.imageWidth = (String) config.getOrDefault("imageWidth", "150");
    }
}
