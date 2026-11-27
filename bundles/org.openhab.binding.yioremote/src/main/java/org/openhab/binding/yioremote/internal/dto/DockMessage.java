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
package org.openhab.binding.yioremote.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DockMessage} is a message received from the YIO Dock.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class DockMessage {
    public @Nullable String type;
    public @Nullable String message;
    public @Nullable String command;
    public @Nullable String code;
    public @Nullable Boolean success;
    public @Nullable String version;
}
