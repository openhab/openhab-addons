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
package org.openhab.binding.dirigera.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ColorLightConfiguration} configuration for lights with temperature or color attributes
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ColorLightConfiguration extends BaseDeviceConfiguration {

    public int fadeTime = 750;
    public int fadeSequence = 0;
}
