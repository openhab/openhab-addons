/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.util.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.Configuration;

/**
 * Simple wrapper containing a Configuration object and whether any changes were detected.
 * This design allows adding new configuration parameters without changing this class.
 *
 * @param configuration The configuration values (never null)
 * @param hasChanges Whether any changes were detected compared to the original configuration
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public record ConfigurationUpdate(Configuration configuration, boolean hasChanges) {
}
