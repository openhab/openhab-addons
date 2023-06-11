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
package org.openhab.binding.pixometer.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data class representing the user configurable settings of a meter thing
 *
 * @author Jerome Luckenbach - Initial contribution
 */
@NonNullByDefault
public class PixometerMeterConfiguration {

    /**
     * The resourceId of the current meter
     */
    public @NonNullByDefault({}) String resourceId;
}
