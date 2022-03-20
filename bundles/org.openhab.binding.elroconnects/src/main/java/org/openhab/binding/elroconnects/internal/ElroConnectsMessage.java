/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.elroconnects.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.util.ElroConnectsUtil;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ElroConnectsMessage} represents the JSON messages exchanged with the ELRO Connects K1 Connector. This
 * class is used to serialize/deserialize the messages.
 *
 * @author Mark Herwege - Initial contribution
 */
@SuppressWarnings("unused") // Suppress warning on serialized fields
@NonNullByDefault
public class ElroConnectsMessage {

    private static class Data {
        private int cmdId;

        @SerializedName(value = "device_ID")
        private @Nullable Integer deviceId;

        @SerializedName(value = "device_name")
        private @Nullable String deviceName;

        @SerializedName(value = "device_status")
        private @Nullable String deviceStatus;

        @SerializedName(value = "answer_content")
        private @Nullable String answerContent;

        @SerializedName(value = "sence_group")
        private @Nullable Integer sceneGroup;

        @SerializedName(value = "scene_type")
        private @Nullable Integer sceneType;

        @SerializedName(value = "scene_content")
        private @Nullable String sceneContent;
    }

    private static class Params {
        private String devTid = "";
        private String ctrlKey = "";
        private Data data = new Data();
    }

    private int msgId;
    private String action = "appSend";
    private Params params = new Params();

    public ElroConnectsMessage(int msgId, String devTid, String ctrlKey, int cmdId) {
        this.msgId = msgId;
        params.devTid = devTid;
        params.ctrlKey = ctrlKey;
        params.data.cmdId = cmdId;
    }

    public ElroConnectsMessage(int msgId) {
        this.msgId = msgId;
        action = "heartbeat";
    }

    public ElroConnectsMessage withDeviceStatus(String deviceStatus) {
        params.data.deviceStatus = deviceStatus;
        return this;
    }

    public ElroConnectsMessage withDeviceId(int deviceId) {
        params.data.deviceId = deviceId;
        return this;
    }

    public ElroConnectsMessage withSceneType(int sceneType) {
        params.data.sceneType = sceneType;
        return this;
    }

    public ElroConnectsMessage withSceneGroup(int sceneGroup) {
        params.data.sceneGroup = sceneGroup;
        return this;
    }

    public ElroConnectsMessage withSceneContent(String sceneContent) {
        params.data.sceneContent = sceneContent;
        return this;
    }

    public ElroConnectsMessage withAnswerContent(String answerContent) {
        params.data.answerContent = answerContent;
        return this;
    }

    public int getMsgId() {
        return msgId;
    }

    public String getAction() {
        return action;
    }

    public int getCmdId() {
        return params.data.cmdId;
    }

    public String getDeviceStatus() {
        return ElroConnectsUtil.stringOrEmpty(params.data.deviceStatus);
    }

    public int getSceneGroup() {
        return ElroConnectsUtil.intOrZero(params.data.sceneGroup);
    }

    public int getSceneType() {
        return ElroConnectsUtil.intOrZero(params.data.sceneType);
    }

    public String getSceneContent() {
        return ElroConnectsUtil.stringOrEmpty(params.data.sceneContent);
    }

    public String getAnswerContent() {
        return ElroConnectsUtil.stringOrEmpty(params.data.answerContent);
    }

    public int getDeviceId() {
        return ElroConnectsUtil.intOrZero(params.data.deviceId);
    }

    public String getDeviceName() {
        return ElroConnectsUtil.stringOrEmpty(params.data.deviceName);
    }
}
