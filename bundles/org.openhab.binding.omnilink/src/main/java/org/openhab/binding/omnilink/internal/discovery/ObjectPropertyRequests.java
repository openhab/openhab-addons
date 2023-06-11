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
package org.openhab.binding.omnilink.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AccessControlReaderProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioSourceProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioZoneProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ButtonProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ThermostatProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ZoneProperties;

/**
 * @author Craig Hamilton - Initial contribution
 *
 * @param <T>
 */
@NonNullByDefault
public class ObjectPropertyRequests<T extends ObjectProperties> {

    public static final ObjectPropertyRequests<ThermostatProperties> THERMOSTAT = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_THERMO, ThermostatProperties.class);

    public static final ObjectPropertyRequests<ButtonProperties> BUTTONS = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_BUTTON, ButtonProperties.class);

    public static final ObjectPropertyRequests<AreaProperties> AREA = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AREA, AreaProperties.class);

    public static final ObjectPropertyRequests<ZoneProperties> ZONE = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_ZONE, ZoneProperties.class);

    public static final ObjectPropertyRequests<UnitProperties> UNIT = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_UNIT, UnitProperties.class);

    public static final ObjectPropertyRequests<AudioZoneProperties> AUDIO_ZONE = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AUDIO_ZONE, AudioZoneProperties.class);

    public static final ObjectPropertyRequests<AudioSourceProperties> AUDIO_SOURCE = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AUDIO_SOURCE, AudioSourceProperties.class);

    public static final ObjectPropertyRequests<AuxSensorProperties> AUX_SENSORS = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AUX_SENSOR, AuxSensorProperties.class);

    public static final ObjectPropertyRequests<AccessControlReaderProperties> LOCK = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_CONTROL_READER, AccessControlReaderProperties.class);

    private final int propertyRequest;
    private final Class<T> responseType;

    private ObjectPropertyRequests(int propertyRequest, Class<T> type) {
        this.propertyRequest = propertyRequest;
        this.responseType = type;
    }

    public int getPropertyRequest() {
        return propertyRequest;
    }

    public Class<T> getResponseType() {
        return responseType;
    }
}
