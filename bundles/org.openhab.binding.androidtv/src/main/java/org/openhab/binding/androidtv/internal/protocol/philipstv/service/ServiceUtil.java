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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.DataDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.NodesDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.TvSettingsCurrentDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.TvSettingsUpdateDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.ValueDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.ValuesDto;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Util class for common used methods from philips tv services
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
final class ServiceUtil {

    private ServiceUtil() {
    }

    static String createTvSettingsRetrievalJson(int nodeId) throws JsonProcessingException {
        NodesDto nodes = new NodesDto();
        nodes.setNodeid(nodeId);
        TvSettingsCurrentDto tvSettingCurrent = new TvSettingsCurrentDto(Collections.singletonList(nodes));
        return OBJECT_MAPPER.writeValueAsString(tvSettingCurrent);
    }

    static String createTvSettingsUpdateJson(int nodeId, int valueToSet) throws JsonProcessingException {
        DataDto data = new DataDto(valueToSet);
        ValueDto value = new ValueDto(data);
        value.setNodeid(nodeId);
        ValuesDto values = new ValuesDto(value);
        values.setValue(value);
        TvSettingsUpdateDto tvSetting = new TvSettingsUpdateDto(Collections.singletonList(values));
        return OBJECT_MAPPER.writeValueAsString(tvSetting);
    }
}
