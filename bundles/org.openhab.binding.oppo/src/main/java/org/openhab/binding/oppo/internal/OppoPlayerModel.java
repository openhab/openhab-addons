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
package org.openhab.binding.oppo.internal;

import static org.openhab.binding.oppo.internal.OppoBindingConstants.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.oppo.internal.communication.OppoCommand;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OppoPlayerModel} enumeration defines the supported Oppo player models.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum OppoPlayerModel {
    /**
     * OPPO BDP-83 Blu-ray player
     */
    BDP83(83, THING_TYPE_BDP83, BDP83_PORT, OppoCommand.QUERY_COMMANDS_83, true),
    /**
     * OPPO BDP-93/95 Blu-ray Player
     */
    BDP93(93, THING_TYPE_BDP93, BDP10X_PORT, OppoCommand.QUERY_COMMANDS_83, true),
    /**
     * OPPO BDP-103/103D Blu-ray Player
     */
    BDP103(103, THING_TYPE_BDP103, BDP10X_PORT, OppoCommand.QUERY_COMMANDS_10X, true),
    /**
     * OPPO BDP-105/105D Blu-ray Player
     */
    BDP105(105, THING_TYPE_BDP105, BDP10X_PORT, OppoCommand.QUERY_COMMANDS_10X, true),
    /**
     * OPPO UDP-203 Ultra HD Blu-ray Player
     */
    UDP203(203, THING_TYPE_UDP203, UDP20X_PORT, OppoCommand.QUERY_COMMANDS_20X, false),
    /**
     * OPPO UDP-205 Ultra HD Blu-ray Player
     */
    UDP205(205, THING_TYPE_UDP205, UDP20X_PORT, OppoCommand.QUERY_COMMANDS_20X, false);

    private final int modelNumber;
    private final ThingTypeUID thingTypeUID;
    private final int port;
    private final Set<OppoCommand> queryCommands;
    private final boolean needsHdmiModeWorkaround;

    OppoPlayerModel(int model, ThingTypeUID thingTypeUID, int port, Set<OppoCommand> queryCommands,
            boolean needsHdmiModeWorkaround) {
        this.modelNumber = model;
        this.thingTypeUID = thingTypeUID;
        this.port = port;
        this.queryCommands = queryCommands;
        this.needsHdmiModeWorkaround = needsHdmiModeWorkaround;
    }

    public int getModelNumber() {
        return modelNumber;
    }

    public String getModelNumberAsString() {
        return Integer.toString(modelNumber);
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public int getPort() {
        return port;
    }

    public Set<OppoCommand> getQueryCommands() {
        return queryCommands;
    }

    public boolean needsHdmiModeWorkaround() {
        return needsHdmiModeWorkaround;
    }

    private static final Map<Integer, OppoPlayerModel> BY_MODEL_NUMBER = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(playerModel -> playerModel.modelNumber, playerModel -> playerModel));

    private static final Map<ThingTypeUID, OppoPlayerModel> BY_THING_TYPE_UID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(playerModel -> playerModel.thingTypeUID, playerModel -> playerModel));

    public static OppoPlayerModel fromModelNumber(int modelNumber) {
        OppoPlayerModel playerModel = BY_MODEL_NUMBER.get(modelNumber);
        if (playerModel == null) {
            throw new IllegalArgumentException("Unknown OPPO model: " + modelNumber);
        }
        return playerModel;
    }

    public static OppoPlayerModel fromThingTypeUID(ThingTypeUID thingTypeUID) {
        OppoPlayerModel playerModel = BY_THING_TYPE_UID.get(thingTypeUID);
        if (playerModel == null) {
            throw new IllegalArgumentException("Unknown OPPO ThingTypeUID: " + thingTypeUID);
        }
        return playerModel;
    }
}
