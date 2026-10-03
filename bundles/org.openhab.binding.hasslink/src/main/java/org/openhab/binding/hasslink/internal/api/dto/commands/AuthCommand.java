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

/**
 * The {@link AuthCommand} sends the Long-Lived Access Token to Home Assistant in response to
 * an {@code auth_required} frame.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class AuthCommand extends BaseCommand {

    public final String accessToken;

    public AuthCommand(String accessToken) {
        super("auth");
        this.accessToken = accessToken;
    }
}
