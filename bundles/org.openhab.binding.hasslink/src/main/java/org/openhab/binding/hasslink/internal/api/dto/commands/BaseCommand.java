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
package org.openhab.binding.hasslink.internal.api.dto.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link BaseCommand} is the base class for all outgoing WebSocket command frames sent to Home Assistant.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public abstract class BaseCommand {

    public @Nullable Integer id;
    public final String type;

    protected BaseCommand(String type) {
        this.type = type;
    }

    protected BaseCommand(int id, String type) {
        this.id = id;
        this.type = type;
    }
}
