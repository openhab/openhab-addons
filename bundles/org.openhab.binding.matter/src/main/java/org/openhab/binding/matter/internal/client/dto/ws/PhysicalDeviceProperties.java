/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.client.dto.ws;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Mirror of matter.js {@code PhysicalDeviceProperties}, forwarded verbatim by the bridge
 * on {@link NodeStateMessage} Connected events. Used to classify Intermittently Connected
 * (sleepy) devices and tune reconnect / re-enumeration behavior.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class PhysicalDeviceProperties {
    public @Nullable Boolean supportsThread;
    public @Nullable Boolean supportsWifi;
    public @Nullable Boolean supportsEthernet;
    public int @Nullable [] rootEndpointServerList;
    public @Nullable Boolean isMainsPowered;
    public @Nullable Boolean isBatteryPowered;
    public @Nullable Boolean isIntermittentlyConnected;
    public @Nullable Boolean isThreadSleepyEndDevice;
    public @Nullable Boolean threadActive;
    public @Nullable BigInteger threadPan;
    public @Nullable Integer threadChannel;
}
