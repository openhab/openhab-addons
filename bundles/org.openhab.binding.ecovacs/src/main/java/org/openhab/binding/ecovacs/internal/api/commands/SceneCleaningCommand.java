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
package org.openhab.binding.ecovacs.internal.api.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class SceneCleaningCommand extends AbstractAreaCleaningCommand {
    public SceneCleaningCommand(String sceneDefinition, int cleanPasses) {
        super("sceneClean", sceneDefinition, cleanPasses);
    }
}
