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
 * Command to retrieve the Home Assistant core configuration snapshot.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class GetConfigCommand extends BaseCommand {

    public static final String TYPE = "get_config";

    public GetConfigCommand(int id) {
        super(id, TYPE);
    }
}
