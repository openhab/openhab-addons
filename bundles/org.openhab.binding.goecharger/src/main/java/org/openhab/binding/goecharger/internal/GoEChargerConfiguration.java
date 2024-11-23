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
package org.openhab.binding.goecharger.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GoEChargerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Samuel Brucksch - Initial contribution
 * @author Reinhard Plaim - Add apiVersion
 */
@NonNullByDefault
public class GoEChargerConfiguration {

    public @Nullable String ip;
    public Integer refreshInterval = 5;
    public Integer apiVersion = 1;
}
