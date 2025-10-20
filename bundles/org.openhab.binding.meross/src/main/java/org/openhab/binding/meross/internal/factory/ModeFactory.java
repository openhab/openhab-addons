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
package org.openhab.binding.meross.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.command.MerossCommand;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link ModeFactory} class is responsible for implementing command mode
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Add state update
 */
@NonNullByDefault
public abstract class ModeFactory {
    public abstract MerossCommand commandMode(Command command, @Nullable Integer deviceChannel);

    public abstract State state(int merossState);
}
