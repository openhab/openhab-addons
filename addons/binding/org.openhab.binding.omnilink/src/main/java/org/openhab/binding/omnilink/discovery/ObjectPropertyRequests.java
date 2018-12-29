/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.discovery;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioSourceProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioZoneProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ButtonProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ConsoleProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.LockProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ThermostatProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ZoneProperties;

/**
 *
 * @author Craig Hamilton
 *
 * @param <T>
 */
public class ObjectPropertyRequests<T extends ObjectProperties> {

    public final static ObjectPropertyRequests<ThermostatProperties> THERMOSTAT = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_THERMO, ThermostatProperties.class);

    public final static ObjectPropertyRequests<ButtonProperties> BUTTONS = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_BUTTON, ButtonProperties.class);

    public final static ObjectPropertyRequests<ConsoleProperties> CONSOLE = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_CONSOLE, ConsoleProperties.class);

    public final static ObjectPropertyRequests<AreaProperties> AREA = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AREA, AreaProperties.class);

    public final static ObjectPropertyRequests<ZoneProperties> ZONE = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_ZONE, ZoneProperties.class);

    public final static ObjectPropertyRequests<UnitProperties> UNIT = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_UNIT, UnitProperties.class);

    public final static ObjectPropertyRequests<AudioZoneProperties> AUDIO_ZONE = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AUDIO_ZONE, AudioZoneProperties.class);

    public final static ObjectPropertyRequests<AudioSourceProperties> AUDIO_SOURCE = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AUDIO_SOURCE, AudioSourceProperties.class);

    public static final ObjectPropertyRequests<AuxSensorProperties> AUX_SENSORS = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_AUX_SENSOR, AuxSensorProperties.class);

    public static final ObjectPropertyRequests<LockProperties> LOCK = new ObjectPropertyRequests<>(
            Message.OBJ_TYPE_CONTROL_READER, LockProperties.class);

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
