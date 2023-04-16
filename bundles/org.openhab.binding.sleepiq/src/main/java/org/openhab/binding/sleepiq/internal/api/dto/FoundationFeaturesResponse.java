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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoundationFeaturesResponse} holds the features of the foundation
 * returned from the sleepiq API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class FoundationFeaturesResponse {
    private static final int BOARD_AS_SINGLE = 0x01;
    private static final int MASSAGE_AND_LIGHT = 0x02;
    private static final int FOOT_CONTROL = 0x04;
    private static final int FOOT_WARMING = 0x08;
    private static final int UNDER_BED_LIGHT = 0x10;

    @SerializedName("fsBedType")
    private int bedType;

    @SerializedName("fsBoardFaults")
    private int boardFaults;

    @SerializedName("fsBoardFeatures")
    private int boardFeatures;

    @SerializedName("fsBoardHWRevisionCode")
    private int boardHWRev;

    @SerializedName("fsBoardStatus")
    private int boardStatus;

    @SerializedName("fsLeftUnderbedLightPWM")
    private int leftUnderbedLightPWM;

    @SerializedName("fsRightUnderbedLightPWM")
    private int rightUnderbedLightPWM;

    public int getBedType() {
        return bedType;
    }

    public int getBoardFaults() {
        return boardFaults;
    }

    public int getBoardFeatures() {
        return boardFeatures;
    }

    public boolean isBoardAsSingle() {
        return (boardFeatures & BOARD_AS_SINGLE) > 0;
    }

    public boolean hasMassageAndLight() {
        return (boardFeatures & MASSAGE_AND_LIGHT) > 0;
    }

    public boolean hasFootControl() {
        return (boardFeatures & FOOT_CONTROL) > 0;
    }

    public boolean hasFootWarming() {
        return (boardFeatures & FOOT_WARMING) > 0;
    }

    public boolean hasUnderBedLight() {
        return (boardFeatures & UNDER_BED_LIGHT) > 0;
    }

    public int getBoardHWRev() {
        return boardHWRev;
    }

    public int getBoardStatus() {
        return boardStatus;
    }

    public int getLeftUnderbedLightPWM() {
        return leftUnderbedLightPWM;
    }

    public int getRightUnderbedLightPWM() {
        return rightUnderbedLightPWM;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FoundationFeaturesResponse [");
        builder.append("bedType=");
        builder.append(bedType);
        builder.append(", boardFaults=");
        builder.append(boardFaults);
        builder.append(", boardFeatures=");
        builder.append(boardFeatures);
        builder.append(", boardHWRevisionCode=");
        builder.append(boardHWRev);
        builder.append(", boardStatus=");
        builder.append(boardStatus);
        builder.append(", leftUnderbedLightPWM=");
        builder.append(leftUnderbedLightPWM);
        builder.append(", rightUnderbedLightPWM=");
        builder.append(rightUnderbedLightPWM);
        builder.append("]");
        return builder.toString();
    }
}
