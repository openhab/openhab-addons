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
package org.openhab.binding.bluetooth.listener.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ListenerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author VitaTucek - Initial contribution
 */
@NonNullByDefault
public class ListenerConfiguration {
    /**
     * Device address
     */
    public String address = "";
    /**
     * Create raw discovered channels automatically
     */
    public boolean autoChannelCreation = false;
    /**
     * Byte order of the numeric channels
     */
    public boolean changeByteOrder = false;
    /**
     * Time in minutes before device timeout is set
     */
    public int dataTimeout = 1;
}
