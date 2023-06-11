/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.handler.LeapBridgeHandler;
import org.openhab.binding.lutron.internal.protocol.leap.LeapCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;

/**
 * Lutron command abstract base class
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public abstract class LutronCommandNew {
    public final TargetType targetType;
    protected final LutronOperation operation;
    protected final LutronCommandType commandType;
    protected final @Nullable Integer integrationId;

    public LutronCommandNew(TargetType targetType, LutronOperation operation, LutronCommandType type,
            @Nullable Integer integrationId) {
        this.targetType = targetType;
        this.operation = operation;
        this.commandType = type;
        this.integrationId = integrationId;
    }

    public LutronCommandType getType() {
        return commandType;
    }

    public LutronOperation getOperation() {
        return operation;
    }

    public @Nullable Integer getIntegrationId() {
        return integrationId;
    }

    public abstract String lipCommand();

    public abstract @Nullable LeapCommand leapCommand(LeapBridgeHandler bridgeHandler, @Nullable Integer leapZone);
}
