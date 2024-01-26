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
package org.openhab.binding.ipcamera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ReolinkState} DTO holds the state and GSON parsed replies from a Reolink Camera.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class ReolinkState {
    public class GetAiStateResponse {
        public class Value {
            public class Alarm {
                @SerializedName(value = "alarmState", alternate = { "alarm_state" }) // alarm_state is used in json
                public int alarmState = 0;
            }

            @SerializedName(value = "dogCat", alternate = { "dog_cat" }) // dog_cat is used in json
            public Alarm dogCat = new Alarm();
            public Alarm face = new Alarm();
            public Alarm people = new Alarm();
            public Alarm vehicle = new Alarm();
        }

        public class Error {
            public String detail = "";
        }

        public Value value = new Value();
        public Error error = new Error();
    }

    public class GetAbilityResponse {
        public class Value {
            public class Ability {
                public class AbilityKey {
                    public int permit = 0;
                    public int ver = 0;
                }

                public class AbilityChn {
                    public AbilityKey supportAiFace = new AbilityKey();
                    public AbilityKey supportAiPeople = new AbilityKey();
                    public AbilityKey supportAiVehicle = new AbilityKey();
                    public AbilityKey supportAiDogCat = new AbilityKey();
                }

                public AbilityChn[] abilityChn = new AbilityChn[1];
                public AbilityKey push = new AbilityKey();
                public AbilityKey scheduleVersion = new AbilityKey();
                public AbilityKey supportAudioAlarm = new AbilityKey();
                public AbilityKey supportAudioAlarmEnable = new AbilityKey();
                public AbilityKey supportEmailEnable = new AbilityKey();
                public AbilityKey supportFtpEnable = new AbilityKey();
                public AbilityKey supportRecordEnable = new AbilityKey();
            }

            @SerializedName(value = "ability", alternate = { "Ability" }) // uses uppercase A
            public Ability ability = new Ability();
        }

        public class Error {
            public String detail = "";
        }

        public Value value = new Value();
        public Error error = new Error();
    }
}
