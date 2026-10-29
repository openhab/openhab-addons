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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Check-in state of an intermittently connected (sleepy) device. Only devices that support long idle times (LIT) send
 * check-ins.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class IcdState {
    public boolean litSupported;
    /** Whether the controller is registered on the device to receive its check-ins */
    public boolean registered;
    /** Time of the last check-in in milliseconds since the epoch, unknown after a restart */
    public @Nullable Long lastCheckIn;
}
