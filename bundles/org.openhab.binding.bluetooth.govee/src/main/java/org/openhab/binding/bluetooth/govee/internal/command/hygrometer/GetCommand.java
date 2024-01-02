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
package org.openhab.binding.bluetooth.govee.internal.command.hygrometer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public abstract class GetCommand extends GoveeCommand {

    @Override
    public byte getCommandType() {
        return READ_TYPE;
    }

    @Override
    protected byte @Nullable [] getData() {
        return null;
    }
}
