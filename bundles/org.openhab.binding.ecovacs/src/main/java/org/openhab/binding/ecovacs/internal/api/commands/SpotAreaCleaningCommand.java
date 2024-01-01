/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class SpotAreaCleaningCommand extends AbstractAreaCleaningCommand {
    public SpotAreaCleaningCommand(List<String> roomIds, int cleanPasses) {
        super("spotArea", String.join(",", roomIds), cleanPasses);
    }
}
