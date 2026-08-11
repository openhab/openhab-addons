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
package org.openhab.binding.ecovacs.internal.api.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for GOAT mower action commands (start, stop, pause, resume).
 * Overrides the payload header version to "0.0.22" which is what the official
 * Ecovacs app sends. The mower firmware uses the 'ver' field to select its parser;
 * with the library default of "0.0.50" the firmware rejects commands with error code
 * 20003 "unknow type".
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMowerCommand extends AbstractNoResponseCommand {

    @Override
    protected String getHeaderVersion() {
        return "0.0.22";
    }
}
