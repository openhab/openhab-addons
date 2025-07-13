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
package org.openhab.binding.zwavejs.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ZwaveJSChannelConfiguration} class contains fields mapping channel configuration parameters.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSChannelConfiguration {
    public @Nullable String incomingUnit;
    public int commandClassId = 0;
    public String commandClassName = "";
    public int endpoint = 0;
    public @Nullable Integer propertyKeyInt;
    public @Nullable String propertyKeyStr;
    public @Nullable String readProperty;
    public @Nullable Integer writePropertyInt;
    public @Nullable String writePropertyStr;
    public boolean inverted = false;
    public double factor = 1.0;
}
