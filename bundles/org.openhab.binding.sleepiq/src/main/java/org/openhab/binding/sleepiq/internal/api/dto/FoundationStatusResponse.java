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
package org.openhab.binding.sleepiq.internal.api.dto;

import org.openhab.binding.sleepiq.internal.api.enums.FoundationPreset;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoundationStatusResponse} holds the status of the foundation
 * returned from the sleepiq API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class FoundationStatusResponse {
    @SerializedName("fsType")
    private String type;

    @SerializedName("fsRightHeadPosition")
    private FoundationPosition rightHeadPosition;

    @SerializedName("fsRightFootPosition")
    private FoundationPosition rightFootPosition;

    @SerializedName("fsLeftHeadPosition")
    private FoundationPosition leftHeadPosition;

    @SerializedName("fsLeftFootPosition")
    private FoundationPosition leftFootPosition;

    @SerializedName("fsCurrentPositionPresetRight")
    private FoundationPreset currentPositionPresetRight;

    @SerializedName("fsCurrentPositionPresetLeft")
    private FoundationPreset currentPositionPresetLeft;

    @SerializedName("fsOutletsOn")
    private boolean outletsOn;

    public String getType() {
        return type;
    }

    public int getRightHeadPosition() {
        return rightHeadPosition.getFoundationPosition().intValue();
    }

    public int getLeftHeadPosition() {
        return leftHeadPosition.getFoundationPosition().intValue();
    }

    public int getRightFootPosition() {
        return rightFootPosition.getFoundationPosition().intValue();
    }

    public int getLeftFootPosition() {
        return leftFootPosition.getFoundationPosition().intValue();
    }

    public FoundationPreset getCurrentPositionPresetRight() {
        return currentPositionPresetRight;
    }

    public FoundationPreset getCurrentPositionPresetLeft() {
        return currentPositionPresetLeft;
    }

    public boolean getOutletsOn() {
        return outletsOn;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FoundationStatusResponse [");
        builder.append("type=");
        builder.append(type);
        builder.append("rightHeadPosition=");
        builder.append(rightHeadPosition);
        builder.append(", leftHeadPosition=");
        builder.append(leftHeadPosition);
        builder.append(", rightFootPosition=");
        builder.append(rightFootPosition);
        builder.append(", leftFootPosition=");
        builder.append(leftFootPosition);
        builder.append(", currentPositionPresetRight=");
        builder.append(currentPositionPresetRight);
        builder.append(", currentPositionPresetLeft=");
        builder.append(currentPositionPresetLeft);
        builder.append(", outletsOn=");
        builder.append(outletsOn);
        builder.append("]");
        return builder.toString();
    }
}
