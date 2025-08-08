/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link CustomerHistoryRecordVoiceTO} encapsulates a voice history record of a customer history
 *
 * @author Jan N. Klug - Initial contribution
 */
public class CustomerHistoryRecordVoiceTO {
    public String recordItemKey;
    public String recordItemType;
    public String utteranceId;
    public long timestamp;
    public String transcriptText;
    public String agentVisualName;
    public List<Object> personsInfo = List.of();

    @Override
    public @NonNull String toString() {
        return "CustomerHistoryRecordVoiceTO{recordItemKey='" + recordItemKey + "', recordItemType='" + recordItemType
                + "', utteranceId='" + utteranceId + "', timestamp=" + timestamp + ", transcriptText='" + transcriptText
                + "', agentVisualName='" + agentVisualName + "', personsInfo=" + personsInfo + "}";
    }
}
