/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.webhook;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NAWebhookCameraEvent} is responsible to hold
 * data given back by the Netatmo API when calling the webhook
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class NAWebhookCameraEvent {

    public static final String PUSH_TYPE_DOORBELL_PRESS = "NDB-rtc";

    @SerializedName("event_type")
    private EventTypeEnum eventType;
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("camera_id")
    private String cameraId;
    @SerializedName("home_id")
    private String homeId;
    @SerializedName("push_type")
    private String pushType;
    @SerializedName("snapshot_url")
    private String snapshotURL;
    @SerializedName("persons")
    private List<NAWebhookCameraEventPerson> persons = new ArrayList<>();

    public EventTypeEnum getEventType() {
        return eventType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getHomeId() {
        return homeId;
    }

    public String getSnapshotURL() {
        return snapshotURL;
    }

    public String getPushType() {
        return pushType;
    }

    public List<NAWebhookCameraEventPerson> getPersons() {
        return persons;
    }

    public enum EventTypeEnum {
        @SerializedName("person")
        PERSON("person"),

        @SerializedName("person_away")
        PERSON_AWAY("person_away"),

        @SerializedName("movement")
        MOVEMENT("movement"),

        @SerializedName("outdoor")
        OUTDOOR("outdoor"),

        @SerializedName("connection")
        CONNECTION("connection"),

        @SerializedName("disconnection")
        DISCONNECTION("disconnection"),

        @SerializedName("on")
        ON("on"),

        @SerializedName("off")
        OFF("off"),

        @SerializedName("boot")
        BOOT("boot"),

        @SerializedName("sd")
        SD("sd"),

        @SerializedName("alim")
        ALIM("alim"),

        @SerializedName("daily_summary")
        DAILY_SUMMARY("daily_summary"),

        @SerializedName("new_module")
        NEW_MODULE("new_module"),

        @SerializedName("module_connect")
        MODULE_CONNECT("module_connect"),

        @SerializedName("module_disconnect")
        MODULE_DISCONNECT("module_disconnect"),

        @SerializedName("module_low_battery")
        MODULE_LOW_BATTERY("module_low_battery"),

        @SerializedName("module_end_update")
        MODULE_END_UPDATE("module_end_update"),

        @SerializedName("tag_big_move")
        TAG_BIG_MOVE("tag_big_move"),

        @SerializedName("tag_small_move")
        TAG_SMALL_MOVE("tag_small_move"),

        @SerializedName("tag_uninstalled")
        TAG_UNINSTALLED("tag_uninstalled"),

        @SerializedName("tag_open")
        TAG_OPEN("tag_open"),

        @SerializedName("human")
        HUMAN("human"),

        @SerializedName("animal")
        ANIMAL("animal"),

        @SerializedName("vehicle")
        VEHICLE("vehicle"),

        @SerializedName("incoming_call")
        INCOMING_CALL("incoming_call"),

        @SerializedName("accepted_call")
        ACCEPTED_CALL("accepted_call"),

        @SerializedName("missed_call")
        MISSED_CALL("missed_call"),

        /**
         * This is not an event of the Netatmo API.
         * It is triggered manually when the push_type {@link NAWebhookCameraEvent#PUSH_TYPE_DOORBELL_PRESS} occurs.
         * This event is triggered when the button on the doorbell pressed and it is received about 10 seconds
         * earlier than all other doorbell events.
         */
        @SerializedName("doorbell_press")
        DOORBELL_PRESS("doorbell_press");

        private String value;

        EventTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
