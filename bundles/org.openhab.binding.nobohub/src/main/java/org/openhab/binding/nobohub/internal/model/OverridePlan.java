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
package org.openhab.binding.nobohub.internal.model;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An override is when the normal weekly program is not followed because it is specified by pressing a switch or using
 * an app.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class OverridePlan {

    private final int id;
    private final OverrideMode mode;
    private final OverrideType type;
    private final @Nullable LocalDateTime startTime;
    private final @Nullable LocalDateTime endTime;
    private final OverrideTarget target;
    private final int targetId;

    public OverridePlan(int id, OverrideMode mode, OverrideType type, @Nullable LocalDateTime startTime,
            @Nullable LocalDateTime endTime, OverrideTarget target, int targetId) {
        this.id = id;
        this.mode = mode;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.target = target;
        this.targetId = targetId;
    }

    public static OverridePlan fromH04(String h04) throws NoboDataException {
        String[] parts = h04.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(
                    String.format("Unexpected number of parts from hub on H4 call: %d", parts.length));
        }

        return new OverridePlan(Integer.parseInt(parts[1]), OverrideMode.getByNumber(Integer.parseInt(parts[2])),
                OverrideType.getByNumber(Integer.parseInt(parts[3])), ModelHelper.toJavaDate(parts[4]),
                ModelHelper.toJavaDate(parts[5]), OverrideTarget.getByNumber(Integer.parseInt(parts[6])),
                Integer.parseInt(parts[7]));
    }

    public static OverridePlan fromMode(OverrideMode mode, LocalDateTime date) {
        return new OverridePlan(1, mode, OverrideType.NOW, null, null, OverrideTarget.HUB, -1);
    }

    public String generateCommandString(final String command) {
        return String.join(" ", command, Integer.toString(id), Integer.toString(mode.getNumValue()),
                Integer.toString(type.getNumValue()), ModelHelper.toHubDateMinutes(startTime),
                ModelHelper.toHubDateMinutes(endTime), Integer.toString(target.getNumValue()),
                Integer.toString(targetId));
    }

    public int getId() {
        return id;
    }

    public OverrideMode getMode() {
        return mode;
    }

    public OverrideType getType() {
        return type;
    }

    public @Nullable LocalDateTime startTime() {
        return startTime;
    }

    public @Nullable LocalDateTime endTime() {
        return endTime;
    }

    public OverrideTarget getTarget() {
        return target;
    }

    public int getTargetId() {
        return targetId;
    }
}
