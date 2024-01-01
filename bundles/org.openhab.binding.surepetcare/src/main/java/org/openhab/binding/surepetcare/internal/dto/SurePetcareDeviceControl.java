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
package org.openhab.binding.surepetcare.internal.dto;

import java.util.List;

import org.openhab.binding.surepetcare.internal.utils.SurePetcareDeviceCurfewListTypeAdapterFactory;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcareDeviceControl} class is used to serialize a JSON object to control certain parameters of a
 * device (e.g. locking mode, curfew etc.).
 *
 * @author Rene Scherer - Initial contribution
 * @author Holger Eisold - Added pet feeder status
 */
public class SurePetcareDeviceControl {

    @SerializedName("locking")
    public Integer lockingModeId;
    public Boolean fastPolling;
    @SerializedName("led_mode")
    public Integer ledModeId;
    @SerializedName("pairing_mode")
    public Integer pairingModeId;
    public Bowls bowls;
    public Lid lid;
    @SerializedName("training_mode")
    public Integer trainingModeId;
    @SerializedName("curfew")
    @JsonAdapter(SurePetcareDeviceCurfewListTypeAdapterFactory.class)
    public SurePetcareDeviceCurfewList curfewList;

    public class Bowls {
        public class BowlSettings {
            @SerializedName("food_type")
            public Integer foodId;
            @SerializedName("target")
            public Integer targetId;

            public BowlSettings(Integer foodId, Integer targetId) {
                this.foodId = foodId;
                this.targetId = targetId;
            }
        }

        @SerializedName("settings")
        public List<BowlSettings> bowlSettings;
        @SerializedName("type")
        public Integer bowlId;

        public Bowls(List<BowlSettings> bowlSettings, Integer bowlId) {
            this.bowlSettings = bowlSettings;
            this.bowlId = bowlId;
        }
    }

    public class Lid {
        @SerializedName("close_delay")
        public Integer closeDelayId;

        public Lid(Integer closeDelayId) {
            this.closeDelayId = closeDelayId;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SurePetcareDeviceControl [");
        builder.append("lockingModeId=").append(lockingModeId);
        builder.append(", fastPolling=").append(fastPolling);
        builder.append(", ledModeId=").append(ledModeId);
        builder.append(", pairingModeId=").append(pairingModeId);
        builder.append(", trainingModeId=").append(trainingModeId);
        builder.append(", curfew=").append(curfewList);
        return builder.toString();
    }
}
