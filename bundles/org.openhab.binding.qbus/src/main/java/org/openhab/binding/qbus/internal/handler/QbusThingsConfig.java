/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link QbusThingsConfig} is responible for handling configurations for all things
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusThingsConfig {
    public @Nullable Integer bistabielId;
    public @Nullable Integer dimmerId;
    public @Nullable Integer co2Id;
    public @Nullable Integer rolId;
    public @Nullable Integer sceneId;
    public @Nullable Integer thermostatId;
}
