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
package org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openhab.binding.digitalstrom.internal.lib.GeneralLibConstance;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseSensorValues;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.AssignedSensors;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.SensorValues;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlConfig;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlInternals;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlValues;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.WeatherSensorData;
import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.HttpTransport;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.SimpleRequestBuilder;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants.ClassKeys;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants.FunctionKeys;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants.InterfaceKeys;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants.ParameterKeys;
import org.openhab.binding.digitalstrom.internal.lib.structure.Apartment;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.CachedMeteringValue;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceConfig;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceSceneSpec;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceParameterClassEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.JSONCachedMeteringValueImpl;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.JSONDeviceConfigImpl;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.JSONDeviceSceneSpecImpl;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl.CircuitImpl;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl;
import org.openhab.binding.digitalstrom.internal.lib.structure.impl.JSONApartmentImpl;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DsAPIImpl} is the implementation of the {@link DsAPI}.
 *
 * @author Alexander Betker - initial contributer
 * @author Alex Maier - initial contributer
 * @author Michael Ochel - implements new methods, API updates and change SimpleJSON to GSON, add helper methods and
 *         requests building with constants to {@link SimpleRequestBuilder}
 * @author Matthias Siegele - implements new methods, API updates and change SimpleJSON to GSON, add helper methods and
 *         requests building with constants to {@link SimpleRequestBuilder}
 */
public class DsAPIImpl implements DsAPI {

    private final Logger logger = LoggerFactory.getLogger(DsAPIImpl.class);
    private final HttpTransport transport;

    public static final String QUERY_GET_METERLIST = "/apartment/dSMeters/*(dSID)";

    /**
     * Contains methods where no login is required.
     */
    public static final List<String> METHODS_MUST_NOT_BE_LOGGED_IN = Arrays.asList(FunctionKeys.LOGIN,
            FunctionKeys.REQUEST_APPLICATION_TOKEN, FunctionKeys.VERSION, FunctionKeys.TIME, FunctionKeys.GET_DSID,
            FunctionKeys.LOGOUT, FunctionKeys.LOGIN_APPLICATION);

    /**
     * Create a new {@link DsAPIImpl} with the given {@link HttpTransport}.
     *
     * @param transport for connection, must not be null
     */
    public DsAPIImpl(HttpTransport transport) {
        this.transport = transport;
    }

    /**
     * Creates a new {@link DsAPIImpl} with creating a new {@link HttpTransport}, parameters see
     * {@link HttpTransportImpl#HttpTransportImpl(String, int, int)}.
     *
     * @param uri of the digitalSTROM-Server, must not be null
     * @param connectTimeout to set
     * @param readTimeout to set
     */
    public DsAPIImpl(String uri, int connectTimeout, int readTimeout) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout);
    }

    /**
     * Creates a new {@link DsAPIImpl} with creating a new {@link HttpTransport}, parameters see
     * {@link HttpTransportImpl#HttpTransportImpl(String, int, int, boolean)}.
     *
     * @param uri of the digitalSTROM-Server, must not be null
     * @param connectTimeout to set
     * @param readTimeout to set
     * @param aceptAllCerts yes/no (true/false)
     */
    public DsAPIImpl(String uri, int connectTimeout, int readTimeout, boolean aceptAllCerts) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout, aceptAllCerts);
    }

    private boolean isValidApartmentSceneNumber(int sceneNumber) {
        return (sceneNumber > -1 && sceneNumber < 256);
    }

    private boolean checkBlankField(JsonObject obj, String key) {
        return obj != null && obj.get(key) != null;
    }

    private boolean checkRequiredZone(Integer zoneID, String zoneName) {
        return zoneID != null && zoneID > -1 || (zoneName != null && !zoneName.isBlank());
    }

    private boolean checkRequiredDevice(DSID dsid, String dSUID, String name) {
        String objectString = SimpleRequestBuilder.objectToString(dsid);
        return (objectString != null && !objectString.isBlank()) || (name != null && !name.isBlank())
                || (dSUID != null && !dSUID.isBlank());
    }

    @Override
    public boolean callApartmentScene(String token, Short groupID, String groupName, Scene sceneNumber, Boolean force) {
        if (sceneNumber != null && isValidApartmentSceneNumber(sceneNumber.getSceneNumber())) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                    .addFunction(FunctionKeys.CALL_SCENE).addDefaultGroupParameter(token, groupID, groupName)
                    .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                    .addParameter(ParameterKeys.FORCE, force.toString()).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean undoApartmentScene(String token, Short groupID, String groupName, Scene sceneNumber) {
        if (sceneNumber != null && isValidApartmentSceneNumber(sceneNumber.getSceneNumber())) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                    .addFunction(FunctionKeys.UNDO_SCENE).addDefaultGroupParameter(token, groupID, groupName)
                    .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                    .buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public Apartment getApartmentStructure(String token) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_STRUCTURE).addParameter(ParameterKeys.TOKEN, token).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject apartObj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (checkBlankField(apartObj, JSONApiResponseKeysEnum.APARTMENT.getKey())) {
                return new JSONApartmentImpl((JsonObject) apartObj.get(JSONApiResponseKeysEnum.APARTMENT.getKey()));
            }
        }
        return null;
    }

    @Override
    public List<Device> getApartmentDevices(String token) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_DEVICES).addParameter(ParameterKeys.TOKEN, token).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)
                && responseObj.get(JSONApiResponseKeysEnum.RESULT.getKey()) instanceof JsonArray) {
            JsonArray array = (JsonArray) responseObj.get(JSONApiResponseKeysEnum.RESULT.getKey());

            List<Device> deviceList = new LinkedList<>();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof JsonObject) {
                    deviceList.add(new DeviceImpl((JsonObject) array.get(i)));
                }
            }
            return deviceList;
        }
        return new LinkedList<>();
    }

    @Override
    public List<Circuit> getApartmentCircuits(String sessionToken) {
        String response = transport.execute(
                SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_CIRCUITS)
                        .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (responseObj.get(JSONApiResponseKeysEnum.CIRCUITS.getKey()).isJsonArray()) {
                JsonArray array = responseObj.get(JSONApiResponseKeysEnum.CIRCUITS.getKey()).getAsJsonArray();

                List<Circuit> circuitList = new LinkedList<>();
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i).isJsonObject()) {
                        circuitList.add(new CircuitImpl(array.get(i).getAsJsonObject()));
                    }
                }
                return circuitList;
            }
        }
        return new LinkedList<>();
    }

    @Override
    public boolean callZoneScene(String token, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber, Boolean force) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.ZONE).addFunction(FunctionKeys.CALL_SCENE)
                            .addDefaultZoneGroupParameter(token, zoneID, zoneName, groupID, groupName)
                            .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                            .addParameter(ParameterKeys.FORCE, force.toString()).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean undoZoneScene(String token, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.UNDO_SCENE)
                    .addDefaultZoneGroupParameter(token, zoneID, zoneName, groupID, groupName)
                    .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                    .buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean turnDeviceOn(String token, DSID dsid, String dSUID, String name) {
        if (checkRequiredDevice(dsid, dSUID, name)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.TURN_ON)
                    .addDefaultDeviceParameter(token, dsid, dSUID, name).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean turnDeviceOff(String token, DSID dSID, String dSUID, String name) {
        if (checkRequiredDevice(dSID, dSUID, name)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.TURN_OFF)
                    .addDefaultDeviceParameter(token, dSID, dSUID, name).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

        }
        return false;
    }

    @Override
    public DeviceConfig getDeviceConfig(String token, DSID dSID, String dSUID, String name,
            DeviceParameterClassEnum class_, Integer index) {
        if (checkRequiredDevice(dSID, dSUID, name) && class_ != null
                && SimpleRequestBuilder.objectToString(index) != null) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.DEVICE)
                    .addFunction(FunctionKeys.GET_CONFIG).addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.CLASS, class_.getClassIndex().toString())
                    .addParameter(ParameterKeys.INDEX, SimpleRequestBuilder.objectToString(index))
                    .buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject configObject = JSONResponseHandler.getResultJsonObject(responseObj);

                if (configObject != null) {
                    return new JSONDeviceConfigImpl(configObject);
                }
            }
        }
        return null;
    }

    @Override
    public int getDeviceOutputValue(String token, DSID dSID, String dSUID, String name, Short offset) {
        if (checkRequiredDevice(dSID, dSUID, name) && SimpleRequestBuilder.objectToString(offset) != null) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_OUTPUT_VALUE)
                    .addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.OFFSET, SimpleRequestBuilder.objectToString(offset))
                    .buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                if (checkBlankField(valueObject, JSONApiResponseKeysEnum.VALUE.getKey())) {
                    return valueObject.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsInt();
                }
            }
        }
        return -1;
    }

    @Override
    public boolean setDeviceOutputValue(String token, DSID dSID, String dSUID, String name, Short offset,
            Integer value) {
        if (checkRequiredDevice(dSID, dSUID, name) && SimpleRequestBuilder.objectToString(offset) != null
                && SimpleRequestBuilder.objectToString(value) != null) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.SET_OUTPUT_VALUE)
                    .addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.OFFSET, SimpleRequestBuilder.objectToString(offset))
                    .addParameter(ParameterKeys.VALUE, SimpleRequestBuilder.objectToString(value))
                    .buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public DeviceSceneSpec getDeviceSceneMode(String token, DSID dSID, String dSUID, String name, Short sceneID) {
        if (checkRequiredDevice(dSID, dSUID, name) && SimpleRequestBuilder.objectToString(sceneID) != null) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_SCENE_MODE)
                    .addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.SCENE_ID, SimpleRequestBuilder.objectToString(sceneID))
                    .buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject sceneSpec = JSONResponseHandler.getResultJsonObject(responseObj);

                if (sceneSpec != null) {
                    return new JSONDeviceSceneSpecImpl(sceneSpec);
                }
            }
        }
        return null;
    }

    @Override
    public short getDeviceSensorValue(String token, DSID dSID, String dSUID, String name, Short sensorIndex) {
        if (checkRequiredDevice(dSID, dSUID, dSUID) && sensorIndex != null) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_SENSOR_VALUE)
                    .addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.SENSOR_INDEX, SimpleRequestBuilder.objectToString(sensorIndex))
                    .buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                if (checkBlankField(valueObject, JSONApiResponseKeysEnum.SENSOR_VALUE.getKey())) {
                    return valueObject.get(JSONApiResponseKeysEnum.SENSOR_VALUE.getKey()).getAsShort();
                }
            }
        }
        return -1;
    }

    @Override
    public boolean callDeviceScene(String token, DSID dSID, String dSUID, String name, Scene sceneNumber,
            Boolean force) {
        if (checkRequiredDevice(dSID, dSUID, name) && sceneNumber != null) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.DEVICE)
                    .addFunction(FunctionKeys.CALL_SCENE).addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                    .addParameter(ParameterKeys.FORCE, force.toString()).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean undoDeviceScene(String token, DSID dSID, String dSUID, String name, Scene sceneNumber) {
        if (checkRequiredDevice(dSID, dSUID, name) && sceneNumber != null) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.DEVICE)
                    .addFunction(FunctionKeys.UNDO_SCENE).addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                    .buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean subscribeEvent(String token, String name, Integer subscriptionID, int connectionTimeout,
            int readTimeout) {
        if ((name != null && !name.isBlank()) && SimpleRequestBuilder.objectToString(subscriptionID) != null) {
            String response;
            response = transport.execute(
                    SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.EVENT).addFunction(FunctionKeys.SUBSCRIBE)
                            .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.NAME, name)
                            .addParameter(ParameterKeys.SUBSCRIPTIONID,
                                    SimpleRequestBuilder.objectToString(subscriptionID))
                            .buildRequestString(),
                    connectionTimeout, readTimeout);
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean unsubscribeEvent(String token, String name, Integer subscriptionID, int connectionTimeout,
            int readTimeout) {
        if (name != null && !name.isBlank() && SimpleRequestBuilder.objectToString(subscriptionID) != null) {
            String response;
            response = transport.execute(
                    SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.EVENT).addFunction(FunctionKeys.UNSUBSCRIBE)
                            .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.NAME, name)
                            .addParameter(ParameterKeys.SUBSCRIPTIONID,
                                    SimpleRequestBuilder.objectToString(subscriptionID))
                            .buildRequestString(),
                    connectionTimeout, readTimeout);
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public String getEvent(String token, Integer subscriptionID, Integer timeout) {
        if (SimpleRequestBuilder.objectToString(subscriptionID) != null) {
            return transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.EVENT)
                    .addFunction(FunctionKeys.GET).addParameter(ParameterKeys.TOKEN, token)
                    .addParameter(ParameterKeys.SUBSCRIPTIONID, SimpleRequestBuilder.objectToString(subscriptionID))
                    .addParameter(ParameterKeys.TIMEOUT, SimpleRequestBuilder.objectToString(timeout))
                    .buildRequestString());
        }
        return null;
    }

    @Override
    public int getTime(String token) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.TIME).addParameter(ParameterKeys.TOKEN, token).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);

            if (checkBlankField(obj, JSONApiResponseKeysEnum.TIME.getKey())) {
                return obj.get(JSONApiResponseKeysEnum.TIME.getKey()).getAsInt();
            }
        }
        return -1;
    }

    @Override
    public List<Integer> getResolutions(String token) {
        String response = transport.execute(
                SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.METERING).addFunction(FunctionKeys.GET_RESOLUTIONS)
                        .addParameter(ParameterKeys.TOKEN, token).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject resObj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (resObj != null && resObj.get(JSONApiResponseKeysEnum.RESOLUTIONS.getKey()) instanceof JsonArray) {
                JsonArray array = (JsonArray) resObj.get(JSONApiResponseKeysEnum.RESOLUTIONS.getKey());

                List<Integer> resolutionList = new LinkedList<>();
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JsonObject) {
                        JsonObject jObject = (JsonObject) array.get(i);

                        if (jObject.get(JSONApiResponseKeysEnum.RESOLUTION.getKey()) != null) {
                            int val = jObject.get(JSONApiResponseKeysEnum.RESOLUTION.getKey()).getAsInt();
                            if (val != -1) {
                                resolutionList.add(val);
                            }
                        }
                    }
                }
                return resolutionList;
            }
        }
        return null;
    }

    @Override
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, List<String> meterDSIDs,
            MeteringUnitsEnum unit) {
        if (meterDSIDs != null) {
            String jsonMeterList = ".meters(";
            for (int i = 0; i < meterDSIDs.size(); i++) {
                if (!meterDSIDs.get(i).isEmpty()) {
                    jsonMeterList += meterDSIDs.get(i);
                    if (i < meterDSIDs.size() - 1 && !meterDSIDs.get(i + 1).isEmpty()) {
                        jsonMeterList += ",";
                    } else {
                        break;
                    }
                }
            }
            jsonMeterList += ")";
            return getLatest(token, type, jsonMeterList, unit);
        }
        return null;
    }

    @Override
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, String meterDSIDs,
            MeteringUnitsEnum unit) {
        if (type != null && meterDSIDs != null) {
            String unitstring = null;
            if (unit != null) {
                unitstring = unit.unit;
            }
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.METERING)
                    .addFunction(FunctionKeys.GET_LATEST).addParameter(ParameterKeys.TOKEN, token)
                    .addParameter(ParameterKeys.TYPE, SimpleRequestBuilder.objectToString(type).toLowerCase())
                    .addParameter(ParameterKeys.FROM, meterDSIDs).addParameter(ParameterKeys.UNIT, unitstring)
                    .buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject latestObj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (latestObj != null && latestObj.get(JSONApiResponseKeysEnum.VALUES.getKey()) instanceof JsonArray) {
                    JsonArray array = (JsonArray) latestObj.get(JSONApiResponseKeysEnum.VALUES.getKey());

                    List<CachedMeteringValue> list = new LinkedList<>();
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i) instanceof JsonObject) {
                            list.add(new JSONCachedMeteringValueImpl((JsonObject) array.get(i), type, unit));
                        }
                    }
                    return list;
                }
            }
        }
        return null;
    }

    @Override
    public boolean setDeviceValue(String token, DSID dSID, String dSUID, String name, Integer value) {
        if (checkRequiredDevice(dSID, dSUID, name)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.DEVICE)
                    .addFunction(FunctionKeys.SET_VALUE).addParameter(ParameterKeys.TOKEN, token)
                    .addParameter(ParameterKeys.DSID, SimpleRequestBuilder.objectToString(dSID))
                    .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                    .addParameter(ParameterKeys.VALUE, value.toString()).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public List<String> getMeterList(String token) {
        List<String> meterList = new LinkedList<>();
        JsonObject responseObj = query(token, QUERY_GET_METERLIST);
        if (responseObj != null && responseObj.get(JSONApiResponseKeysEnum.DS_METERS.getKey()).isJsonArray()) {
            JsonArray array = responseObj.get(JSONApiResponseKeysEnum.DS_METERS.getKey()).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof JsonObject) {
                    meterList.add(array.get(i).getAsJsonObject().get("dSID").getAsString());
                }
            }
        }
        return meterList;
    }

    @Override
    public String loginApplication(String loginToken) {
        if (loginToken != null && !loginToken.isBlank()) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.LOGIN_APPLICATION)
                    .addParameter(ParameterKeys.LOGIN_TOKEN, loginToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                String tokenStr = null;

                if (checkBlankField(obj, JSONApiResponseKeysEnum.TOKEN.getKey())) {
                    tokenStr = obj.get(JSONApiResponseKeysEnum.TOKEN.getKey()).getAsString();
                }
                if (tokenStr != null) {
                    return tokenStr;
                }
            }
        }
        return null;
    }

    @Override
    public String login(String user, String password) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.LOGIN).addParameter(ParameterKeys.USER, user)
                .addParameter(ParameterKeys.PASSWORD, password).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            String tokenStr = null;

            if (checkBlankField(obj, JSONApiResponseKeysEnum.TOKEN.getKey())) {
                tokenStr = obj.get(JSONApiResponseKeysEnum.TOKEN.getKey()).getAsString();
            }
            if (tokenStr != null) {
                return tokenStr;
            }
        }

        return null;
    }

    @Override
    public boolean logout() {
        String response;
        response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.LOGOUT).buildRequestString());
        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public Map<String, String> getDSID(String token) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.GET_DSID).addParameter(ParameterKeys.TOKEN, token).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null) {
                Map<String, String> dsidMap = new HashMap<>(obj.entrySet().size());
                for (Entry<String, JsonElement> entry : obj.entrySet()) {
                    dsidMap.put(entry.getKey(), entry.getValue().getAsString());
                }
                return dsidMap;
            }
        }
        return null;
    }

    @Override
    public boolean enableApplicationToken(String applicationToken, String sessionToken) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.ENABLE_APPLICATION_TOKEN).addParameter(ParameterKeys.TOKEN, sessionToken)
                .addParameter(ParameterKeys.APPLICATION_TOKEN, applicationToken).buildRequestString(),
                Config.HIGH_CONNECTION_TIMEOUT, Config.HIGH_READ_TIMEOUT);
        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public String requestAppplicationToken(String applicationName) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.REQUEST_APPLICATION_TOKEN)
                .addParameter(ParameterKeys.APPLICATION_NAME, applicationName).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null) {
                return obj.get(JSONApiResponseKeysEnum.APPLICATION_TOKEN.getKey()).getAsString();
            }
        }
        return null;
    }

    @Override
    public boolean revokeToken(String applicationToken, String sessionToken) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.REVOKE_TOKEN).addParameter(ParameterKeys.APPLICATION_TOKEN, applicationToken)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public int checkConnection(String token) {
        return transport.checkConnection(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_NAME).addParameter(ParameterKeys.TOKEN, token).buildRequestString());
    }

    @Override
    public int[] getSceneValue(String token, DSID dSID, String dSUID, String name, Short sceneId) {
        int[] value = { -1, -1 };
        if (checkRequiredDevice(dSID, dSUID, name)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.DEVICE)
                    .addFunction(FunctionKeys.GET_SCENE_VALUE).addDefaultDeviceParameter(token, dSID, dSUID, name)
                    .addParameter(ParameterKeys.SCENE_ID, SimpleRequestBuilder.objectToString(sceneId))
                    .buildRequestString(), Config.DEFAULT_CONNECTION_TIMEOUT, Config.HIGH_READ_TIMEOUT);
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null && obj.get(JSONApiResponseKeysEnum.VALUE.getKey()) != null) {
                    value[GeneralLibConstance.SCENE_ARRAY_INDEX_VALUE] = obj.get(JSONApiResponseKeysEnum.VALUE.getKey())
                            .getAsInt();
                    if (obj.get(JSONApiResponseKeysEnum.ANGLE.getKey()) != null) {
                        value[GeneralLibConstance.SCENE_ARRAY_INDEX_ANGLE] = obj
                                .get(JSONApiResponseKeysEnum.ANGLE.getKey()).getAsInt();
                    }
                    return value;
                }
            }
        }
        return value;
    }

    @Override
    public boolean increaseValue(String sessionToken, DSID dSID, String dSUID, String name) {
        if (checkRequiredDevice(dSID, dSUID, name)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.INCREASE_VALUE)
                    .addDefaultDeviceParameter(sessionToken, dSID, dSUID, name).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean decreaseValue(String sessionToken, DSID dSID, String dSUID, String name) {
        if (checkRequiredDevice(dSID, dSUID, name)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.DECREASE_VALUE)
                    .addDefaultDeviceParameter(sessionToken, dSID, dSUID, name).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public String getInstallationName(String sessionToken) {
        String response = null;
        try {
            response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_NAME)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
        } catch (Exception e) {
            logger.debug("An exception occurred", e);

        }
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
            }
        }
        return null;
    }

    @Override
    public String getZoneName(String sessionToken, Integer zoneID) {
        if (checkRequiredZone(zoneID, null)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_NAME)
                    .addParameter(ParameterKeys.ID, SimpleRequestBuilder.objectToString(zoneID))
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                    return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
                }
            }
        }
        return null;
    }

    @Override
    public String getDeviceName(String sessionToken, DSID dSID, String dSUID) {
        if (checkRequiredDevice(dSID, dSUID, null)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_NAME)
                    .addDefaultDeviceParameter(sessionToken, dSID, dSUID, null).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                    return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
                }
            }
        }
        return null;
    }

    @Override
    public String getCircuitName(String sessionToken, DSID dSID) {
        String response = transport
                .execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.CIRCUIT).addFunction(FunctionKeys.GET_NAME)
                        .addParameter(ParameterKeys.DSID, SimpleRequestBuilder.objectToString(dSID))
                        .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
            }
        }

        return null;
    }

    @Override
    public String getSceneName(String sessionToken, Integer zoneID, String zoneName, Short groupID, Short sceneID) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SCENE_GET_NAME)
                    .addDefaultZoneGroupParameter(sessionToken, zoneID, zoneName, groupID, null)
                    .addParameter(ParameterKeys.SCENENUMBER, SimpleRequestBuilder.objectToString(sceneID))
                    .buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                    return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
                }
            }
        }
        return null;
    }

    @Override
    public TemperatureControlStatus getZoneTemperatureControlStatus(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.ZONE)
                    .addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_STATUS)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                return new TemperatureControlStatus(obj, zoneID, zoneName);
            }
        }
        return null;
    }

    @Override
    public TemperatureControlConfig getZoneTemperatureControlConfig(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_CONFIG)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                return new TemperatureControlConfig(obj, zoneID, zoneName);
            }
        }
        return null;
    }

    @Override
    public TemperatureControlValues getZoneTemperatureControlValues(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_VALUES)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                return new TemperatureControlValues(obj, zoneID, zoneName);
            }
        }
        return null;
    }

    @Override
    public AssignedSensors getZoneAssignedSensors(String sessionToken, Integer zoneID, String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_ASSIGNED_SENSORS)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                return new AssignedSensors(obj, zoneID, zoneName);
            }
        }
        return null;
    }

    @Override
    public boolean setZoneTemperatureControlState(String sessionToken, Integer zoneID, String controlState,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_TEMEPERATURE_CONTROL_STATE)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName)
                    .addParameter(ParameterKeys.CONTROL_STATE, controlState).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean setZoneTemperatureControlValues(String sessionToken, Integer zoneID, String zoneName,
            List<Object[]> controlValues) {
        if (checkRequiredZone(zoneID, zoneName)) {
            if (checkRequiredZone(zoneID, zoneName)) {
                if (controlValues != null) {
                    SimpleRequestBuilder builder = SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                            .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_TEMEPERATURE_CONTROL_VALUE)
                            .addDefaultZoneParameter(sessionToken, zoneID, zoneName);
                    for (Object[] objAry : controlValues) {
                        if (objAry.length == 2 && objAry[0] instanceof String && objAry[1] instanceof Integer) {
                            builder.addParameter((String) objAry[0], SimpleRequestBuilder.objectToString(objAry[1]));
                        } else {
                            builder.buildRequestString();
                            throw new IllegalArgumentException(
                                    "The first field of the object array have to be a String and the second have to be a Integer.");
                        }
                    }
                    String response = transport.execute(builder.buildRequestString());

                    return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
                }
            }
        }
        return false;
    }

    @Override
    public SensorValues getZoneSensorValues(String sessionToken, Integer zoneID, String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.ZONE)
                    .addFunction(FunctionKeys.GET_SENSOR_VALUES).addParameter(ParameterKeys.TOKEN, sessionToken)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                return new SensorValues(obj, zoneID, zoneName);
            }
        }
        return null;
    }

    @Override
    public boolean setZoneTemperatureControlConfig(String sessionToken, Integer zoneID, String zoneName,
            String controlDSUID, Short controlMode, Integer referenceZone, Float ctrlOffset, Float emergencyValue,
            Float manualValue, Float ctrlKp, Float ctrlTs, Float ctrlTi, Float ctrlKd, Float ctrlImin, Float ctrlImax,
            Float ctrlYmin, Float ctrlYmax, Boolean ctrlAntiWindUp, Boolean ctrlKeepFloorWarm) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_TEMPERATION_CONTROL_CONFIG)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName)
                    .addParameter(ParameterKeys.CONTROL_MODE, SimpleRequestBuilder.objectToString(controlMode))
                    .addParameter(ParameterKeys.CONTROL_DSUID, controlDSUID)
                    .addParameter(ParameterKeys.REFERENCE_ZONE, SimpleRequestBuilder.objectToString(referenceZone))
                    .addParameter(ParameterKeys.CTRL_OFFSET, SimpleRequestBuilder.objectToString(ctrlOffset))
                    .addParameter(ParameterKeys.EMERGENCY_VALUE, SimpleRequestBuilder.objectToString(emergencyValue))
                    .addParameter(ParameterKeys.MANUAL_VALUE, SimpleRequestBuilder.objectToString(manualValue))
                    .addParameter(ParameterKeys.CTRL_KP, SimpleRequestBuilder.objectToString(ctrlKp))
                    .addParameter(ParameterKeys.CTRL_TS, SimpleRequestBuilder.objectToString(ctrlTs))
                    .addParameter(ParameterKeys.CTRL_TI, SimpleRequestBuilder.objectToString(ctrlTi))
                    .addParameter(ParameterKeys.CTRL_KD, SimpleRequestBuilder.objectToString(ctrlKd))
                    .addParameter(ParameterKeys.CTRL_I_MIN, SimpleRequestBuilder.objectToString(ctrlImin))
                    .addParameter(ParameterKeys.CTRL_I_MAX, SimpleRequestBuilder.objectToString(ctrlImax))
                    .addParameter(ParameterKeys.CTRL_Y_MIN, SimpleRequestBuilder.objectToString(ctrlYmin))
                    .addParameter(ParameterKeys.CTRL_Y_MAX, SimpleRequestBuilder.objectToString(ctrlYmax))
                    .addParameter(ParameterKeys.CTRL_ANTI_WIND_UP, SimpleRequestBuilder.objectToString(ctrlAntiWindUp))
                    .addParameter(ParameterKeys.CTRL_KEEP_FLOOR_WARM,
                            SimpleRequestBuilder.objectToString(ctrlKeepFloorWarm))
                    .buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean setZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType,
            DSID dSID) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_SENSOR_SOURCE)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName)
                    .addParameter(ParameterKeys.SENSOR_TYPE, sensorType.getSensorType().toString())
                    .addParameter(ParameterKeys.DSID, SimpleRequestBuilder.objectToString(dSID)).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public boolean clearZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_TEMEPERATURE_CONTROL_VALUE)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName)
                    .addParameter(ParameterKeys.SENSOR_TYPE, sensorType.getSensorType().toString())
                    .buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public TemperatureControlInternals getZoneTemperatureControlInternals(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_INTERNALS)
                    .addDefaultZoneParameter(sessionToken, zoneID, zoneName).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                return new TemperatureControlInternals(obj, zoneID, zoneName);
            }
        }
        return null;
    }

    @Override
    public boolean setZoneOutputValue(String sessionToken, Integer zoneID, String zoneName, Short groupID,
            String groupName, Integer value) {
        if (value != null && checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_OUTPUT_VALUE)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.NAME, zoneName)
                    .addDefaultZoneGroupParameter(sessionToken, zoneID, zoneName, groupID, groupName)
                    .addParameter(ParameterKeys.VALUE, SimpleRequestBuilder.objectToString(value))
                    .buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean zoneBlink(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.BLINK)
                    .addDefaultZoneGroupParameter(sessionToken, zoneID, zoneName, groupID, groupName)
                    .buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pushZoneSensorValue(String sessionToken, Integer zoneID, String zoneName, Short groupID,
            String sourceDSUID, Float sensorValue, SensorEnum sensorType) {
        if (checkRequiredZone(zoneID, zoneName) && sensorType != null && sensorValue != null) {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.ZONE)
                            .addFunction(FunctionKeys.PUSH_SENSOR_VALUE)
                            .addDefaultZoneGroupParameter(sessionToken, zoneID, zoneName, groupID, null)
                            .addParameter(ParameterKeys.SOURCE_DSUID, sourceDSUID)
                            .addParameter(ParameterKeys.SENSOR_VALUE, SimpleRequestBuilder.objectToString(sensorValue))
                            .addParameter(ParameterKeys.SENSOR_TYPE,
                                    SimpleRequestBuilder.objectToString(sensorType.getSensorType()))
                            .buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        }
        return false;
    }

    @Override
    public List<TemperatureControlStatus> getApartmentTemperatureControlStatus(String sessionToken) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_STATUS)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                if (jArray.size() != 0) {
                    List<TemperatureControlStatus> list = new ArrayList<>(jArray.size());
                    Iterator<JsonElement> iter = jArray.iterator();
                    while (iter.hasNext()) {
                        TemperatureControlStatus tContStat = new TemperatureControlStatus(
                                iter.next().getAsJsonObject());
                        list.add(tContStat);
                    }
                    return list;
                }
            }
        }
        return null;
    }

    @Override
    public Map<Integer, TemperatureControlConfig> getApartmentTemperatureControlConfig(String sessionToken) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_CONFIG)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                if (jArray.size() != 0) {
                    Map<Integer, TemperatureControlConfig> map = new HashMap<>(jArray.size());
                    Iterator<JsonElement> iter = jArray.iterator();
                    while (iter.hasNext()) {
                        TemperatureControlConfig tContConf = new TemperatureControlConfig(
                                iter.next().getAsJsonObject());
                        map.put(tContConf.getZoneID(), tContConf);
                    }
                    return map;
                }
            }
        }
        return null;
    }

    @Override
    public Map<Integer, TemperatureControlValues> getApartmentTemperatureControlValues(String sessionToken) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_VALUES)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                if (jArray.size() != 0) {
                    Map<Integer, TemperatureControlValues> map = new HashMap<>(jArray.size());
                    Iterator<JsonElement> iter = jArray.iterator();
                    while (iter.hasNext()) {
                        TemperatureControlValues tContVal = new TemperatureControlValues(iter.next().getAsJsonObject());
                        map.put(tContVal.getZoneID(), tContVal);
                    }
                    return map;
                }
            }
        }
        return null;
    }

    @Override
    public Map<Integer, AssignedSensors> getApartmentAssignedSensors(String sessionToken) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_ASSIGNED_SENSORS).addParameter(ParameterKeys.TOKEN, sessionToken)
                .buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                if (jArray.size() != 0) {
                    HashMap<Integer, AssignedSensors> map = new HashMap<>(jArray.size());
                    Iterator<JsonElement> iter = jArray.iterator();
                    while (iter.hasNext()) {
                        AssignedSensors assignedSensors = new AssignedSensors(iter.next().getAsJsonObject());
                        map.put(assignedSensors.getZoneID(), assignedSensors);
                    }
                    return map;
                }
            }
        }
        return null;
    }

    @Override
    public Map<Integer, BaseSensorValues> getApartmentSensorValues(String sessionToken) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.APARTMENT)
                .addFunction(FunctionKeys.GET_SENSOR_VALUES).addParameter(ParameterKeys.TOKEN, sessionToken)
                .buildRequestString());
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                WeatherSensorData weather = new WeatherSensorData(obj);
                if (jArray.size() != 0) {
                    HashMap<Integer, BaseSensorValues> map = new HashMap<>(jArray.size() + 1);
                    Iterator<JsonElement> iter = jArray.iterator();
                    while (iter.hasNext()) {
                        SensorValues sensorValues = new SensorValues(iter.next().getAsJsonObject());
                        map.put(sensorValues.getZoneID(), sensorValues);
                    }
                    map.put(GeneralLibConstance.BROADCAST_ZONE_GROUP_ID, weather);
                    return map;
                }
            }
        }
        return null;
    }

    @Override
    public JsonObject query(String sessionToken, String query) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.QUERY).addParameter(ParameterKeys.QUERY, query)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            return JSONResponseHandler.getResultJsonObject(responseObj);
        }
        return null;
    }

    @Override
    public JsonObject query2(String sessionToken, String query) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.QUERY2).addParameter(ParameterKeys.QUERY, query)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            return JSONResponseHandler.getResultJsonObject(responseObj);
        }
        return null;
    }

    @Override
    public String propertyTreeGetString(String sessionToken, String path) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.GET_STRING).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
            return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsString();
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetString(String token, String path, String value) {
        String response = transport.execute(
                SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE).addFunction(FunctionKeys.SET_STRING)
                        .addParameter(ParameterKeys.PATH, path).addParameter(ParameterKeys.TOKEN, token)
                        .addParameter(ParameterKeys.VALUE, value).buildRequestString());

        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public JsonArray propertyTreeGetChildren(String sessionToken, String path) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.GET_CHILDREN).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            return responseObj.get(JSONApiResponseKeysEnum.RESULT.getKey()).getAsJsonArray();
        }
        return null;
    }

    @Override
    public Integer propertyTreeGetInteger(String sessionToken, String path) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.GET_INTEGER).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
            return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsInt();
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetInteger(String sessionToken, String path, Integer value) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.SET_INTEGER).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken)
                .addParameter(ParameterKeys.VALUE, SimpleRequestBuilder.objectToString(value)).buildRequestString());

        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public Boolean propertyTreeGetBoolean(String sessionToken, String path) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.GET_BOOLEAN).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
            return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsBoolean();
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetBoolean(String sessionToken, String path, Boolean value) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.SET_BOOLEAN).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken)
                .addParameter(ParameterKeys.VALUE, SimpleRequestBuilder.objectToString(value)).buildRequestString());

        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public String propertyTreeGetType(String sessionToken, String path) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.GET_TYPE).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
            return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsString();
        }
        return null;
    }

    @Override
    public Map<String, Boolean> propertyTreeGetFlages(String sessionToken, String path) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.GET_FLAGS).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
            Set<Entry<String, JsonElement>> flagEntries = responseObj.entrySet();
            Map<String, Boolean> flags = new HashMap<>(flagEntries.size());
            for (Entry<String, JsonElement> flag : flagEntries) {
                flags.put(flag.getKey(), flag.getValue().getAsBoolean());
            }
            return flags;
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetFlag(String sessionToken, String path, String flag, Boolean value) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.SET_FLAG).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.FLAG, flag)
                .addParameter(ParameterKeys.VALUE, SimpleRequestBuilder.objectToString(value)).buildRequestString());

        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public Boolean propertyTreeRemove(String sessionToken, String path) {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.PROPERTY_TREE)
                .addFunction(FunctionKeys.REMOVE).addParameter(ParameterKeys.PATH, path)
                .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public Map<String, String> getSystemVersion() {
        String response = transport.execute(SimpleRequestBuilder.buildNewJsonRequest(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.VERSION).buildRequestString());

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            Set<Entry<String, JsonElement>> entries = JSONResponseHandler.getResultJsonObject(responseObj).entrySet();
            Map<String, String> versions = new HashMap<>(entries.size());
            for (Entry<String, JsonElement> entry : entries) {
                versions.put(entry.getKey(), entry.getValue().getAsString());
            }
            return versions;
        }
        return null;
    }
}
