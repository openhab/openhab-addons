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
package org.openhab.binding.enocean.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class EnOceanActuatorConfig extends EnOceanBaseConfig {

    public int channel;
    public @Nullable Integer senderIdOffset = null;
    public String manufacturerId = "";
    public String teachInType = "";

    public String sendingEEPId = "";

    public int pollingInterval;

    public boolean broadcastMessages;

    public boolean suppressRepeating;
}
