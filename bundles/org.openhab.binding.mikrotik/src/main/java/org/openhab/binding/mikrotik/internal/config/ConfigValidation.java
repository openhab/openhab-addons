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
package org.openhab.binding.mikrotik.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ConfigValidation} interface should be implemented by all config objects, so the thing handlers could
 * change their state properly, based on the config validation result.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public interface ConfigValidation {
    boolean isValid();
}
