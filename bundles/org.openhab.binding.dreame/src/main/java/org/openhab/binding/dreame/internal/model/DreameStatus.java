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
package org.openhab.binding.dreame.internal.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * Snapshot of mower properties returned by the cloud.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameStatus {
    private final Map<DreameProperty, JsonElement> values = new EnumMap<>(DreameProperty.class);
    private @Nullable DreameMowerPose mowerPose;
    private @Nullable DreameMowerTask mowerTask;
    private @Nullable DreameMowerHeartbeat mowerHeartbeat;
    private @Nullable DreameMowerTaskStatus mowerTaskStatus;
    private @Nullable Boolean mowerTaskActive;
    private boolean missionCompleted;

    public void put(DreameProperty property, JsonElement value) {
        values.put(property, value);
    }

    public void setMowerPose(DreameMowerPose mowerPose) {
        this.mowerPose = mowerPose;
    }

    public @Nullable DreameMowerPose mowerPose() {
        return mowerPose;
    }

    public void setMowerTask(DreameMowerTask mowerTask) {
        this.mowerTask = mowerTask;
    }

    public @Nullable DreameMowerTask mowerTask() {
        return mowerTask;
    }

    public void setMowerHeartbeat(DreameMowerHeartbeat mowerHeartbeat) {
        this.mowerHeartbeat = mowerHeartbeat;
    }

    public @Nullable DreameMowerHeartbeat mowerHeartbeat() {
        return mowerHeartbeat;
    }

    public void setMowerTaskStatus(DreameMowerTaskStatus mowerTaskStatus) {
        this.mowerTaskStatus = mowerTaskStatus;
    }

    public @Nullable DreameMowerTaskStatus mowerTaskStatus() {
        return mowerTaskStatus;
    }

    public void setMowerTaskActive(boolean mowerTaskActive) {
        this.mowerTaskActive = mowerTaskActive;
    }

    public @Nullable Boolean mowerTaskActive() {
        return mowerTaskActive;
    }

    public void setMissionCompleted() {
        missionCompleted = true;
    }

    public boolean missionCompleted() {
        return missionCompleted;
    }

    public boolean hasUpdates() {
        return !values.isEmpty() || mowerPose != null || mowerTask != null || mowerHeartbeat != null
                || mowerTaskStatus != null || mowerTaskActive != null || missionCompleted;
    }

    public boolean contains(DreameProperty property) {
        JsonElement value = values.get(property);
        return value != null && !value.isJsonNull();
    }

    public Set<DreameProperty> properties() {
        return Set.copyOf(values.keySet());
    }

    public int integer(DreameProperty property, int fallback) {
        JsonElement value = values.get(property);
        return value == null || value.isJsonNull() ? fallback : value.getAsInt();
    }

    public boolean bool(DreameProperty property, boolean fallback) {
        JsonElement value = values.get(property);
        return value == null || value.isJsonNull() ? fallback : value.getAsBoolean();
    }
}
