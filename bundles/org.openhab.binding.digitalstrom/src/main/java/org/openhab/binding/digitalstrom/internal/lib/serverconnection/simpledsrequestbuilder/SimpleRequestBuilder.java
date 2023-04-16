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
package org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants.ExeptionConstants;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants.InterfaceKeys;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants.ParameterKeys;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;

/**
 * The {@link SimpleRequestBuilder} build a request string.<br>
 * <br>
 * <i><b>Code example</b><br>
 * String requestString = {@link SimpleRequestBuilder}.{@link #buildNewRequest(String)}.<br>
 * <span style="padding-left:14em">{@link #addRequestClass(String)}.<br>
 * </span>
 * <span style="padding-left:14em">{@link #addFunction(String)}.<br>
 * </span>
 * <span style="padding-left:14em">{@link #addParameter(String, String)}. (optional)<br>
 * </span>
 * <span style="padding-left:14em">{@link #addParameter(String, String)}. (optional)<br>
 * </span>
 * <span style="padding-left:14em">{@link #buildRequestString()};<br>
 * </span></i>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SimpleRequestBuilder {

    // states
    private boolean functionIsChosen = false;
    private boolean parameterIsAdded = false;
    private boolean classIsChosen = false;

    private String request;
    private static SimpleRequestBuilder builder;
    private static final Lock LOCK = new ReentrantLock();

    private SimpleRequestBuilder() {
    }

    /**
     * Returns a {@link SimpleRequestBuilder} with the given intefaceKey as chosen request-interface.
     *
     * @param interfaceKey must not be null
     * @return simpleRequestBuilder with chosen interface
     * @throws NullArgumentException if the interfaceKey is null
     */
    public static SimpleRequestBuilder buildNewRequest(String interfaceKey) throws IllegalArgumentException {
        if (builder == null) {
            builder = new SimpleRequestBuilder();
        }
        LOCK.lock();
        return builder.buildNewRequestInt(interfaceKey);
    }

    /**
     * Returns a {@link SimpleRequestBuilder} with the intefaceKey "Json" as chosen request-interface and adds the given
     * requestClass to the request-string.
     *
     * @param requestClassKey must not be null
     * @return simpleRequestBuilder with chosen requestClass
     * @throws IllegalArgumentException if a requestClass is already chosen
     * @throws NullArgumentException if the requestClassKey is null
     */
    public static SimpleRequestBuilder buildNewJsonRequest(String requestClassKey) throws IllegalArgumentException {
        return buildNewRequest(InterfaceKeys.JSON).addRequestClass(requestClassKey);
    }

    private SimpleRequestBuilder buildNewRequestInt(String interfaceKey) {
        if (interfaceKey == null) {
            throw new IllegalArgumentException("interfaceKey is null");
        }
        request = "/" + interfaceKey + "/";
        classIsChosen = false;
        functionIsChosen = false;
        parameterIsAdded = false;
        return this;
    }

    /**
     * Adds a requestClass to the request-string.
     *
     * @param requestClassKey must not be null
     * @return simpleRequestBuilder with chosen requestClass
     * @throws IllegalArgumentException if a requestClass is already chosen
     * @throws NullArgumentException if the requestClassKey is null
     */
    public SimpleRequestBuilder addRequestClass(String requestClassKey) throws IllegalArgumentException {
        return builder.addRequestClassInt(requestClassKey);
    }

    private SimpleRequestBuilder addRequestClassInt(String requestClassKey) {
        if (!classIsChosen && requestClassKey != null) {
            classIsChosen = true;
            request = request + requestClassKey + "/";
        } else {
            if (!classIsChosen) {
                throw new IllegalArgumentException(ExeptionConstants.CLASS_ALREADY_ADDED);
            } else {
                throw new IllegalArgumentException("requestClassKey is null");
            }
        }
        return this;
    }

    /**
     * Adds a function to the request-string.
     *
     * @param functionKey must not be null
     * @return SimpleRequestBuilder with chosen function
     * @throws IllegalArgumentException if a function is already chosen
     * @throws NullArgumentException if the functionKey is null
     */
    public SimpleRequestBuilder addFunction(String functionKey) throws IllegalArgumentException {
        return builder.addFunctionInt(functionKey);
    }

    private SimpleRequestBuilder addFunctionInt(String functionKey) {
        if (!classIsChosen) {
            throw new IllegalArgumentException(ExeptionConstants.NO_CLASS_ADDED);
        }
        if (!functionIsChosen) {
            if (functionKey != null) {
                functionIsChosen = true;
                request = request + functionKey;
            } else {
                throw new IllegalArgumentException("functionKey is null");
            }
        } else {
            throw new IllegalArgumentException(ExeptionConstants.FUNCTION_ALLREADY_ADDED);
        }
        return this;
    }

    /**
     * Adds a parameter to the request-string, if the parameter value is not null.
     *
     * @param parameterKey must not be null
     * @param parameterValue can be null
     * @return SimpleRequestBuilder with added parameter
     * @throws IllegalArgumentException if no class and function added
     * @throws NullArgumentException if the parameterKey is null
     */
    public SimpleRequestBuilder addParameter(String parameterKey, String parameterValue)
            throws IllegalArgumentException {
        return builder.addParameterInt(parameterKey, parameterValue);
    }

    /**
     * Adds the default parameter for zone-requests to the request-string, if the parameter value is not null.
     *
     * @param sessionToken
     * @param zoneID
     * @param zoneName
     * @return SimpleRequestBuilder with added parameter
     * @throws IllegalArgumentException if no class and function added
     * @throws NullArgumentException if the parameterKey is null
     */
    public SimpleRequestBuilder addDefaultZoneParameter(String sessionToken, Integer zoneID, String zoneName)
            throws IllegalArgumentException {
        return addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.ID, objectToString(zoneID))
                .addParameter(ParameterKeys.NAME, zoneName);
    }

    /**
     * Adds a parameter for group-requests t the request-string, if the parameter value is not null.
     *
     * @param sessionToken
     * @param groupID
     * @param groupName
     * @return SimpleRequestBuilder with added parameter
     * @throws IllegalArgumentException if no class and function added
     * @throws NullArgumentException if the parameterKey is null
     */
    public SimpleRequestBuilder addDefaultGroupParameter(String sessionToken, Short groupID, String groupName)
            throws IllegalArgumentException {
        return addParameter(ParameterKeys.TOKEN, sessionToken)
                .addParameter(ParameterKeys.GROUP_ID, objectToString(groupID))
                .addParameter(ParameterKeys.GROUP_NAME, groupName);
    }

    /**
     * Adds a parameter for zone-group-requests t the request-string, if the parameter value is not null.
     *
     * @param sessionToken
     * @param zoneID
     * @param zoneName
     * @param groupID
     * @param groupName
     * @return SimpleRequestBuilder with added parameter
     * @throws IllegalArgumentException if no class and function added
     * @throws NullArgumentException if the parameterKey is null
     */
    public SimpleRequestBuilder addDefaultZoneGroupParameter(String sessionToken, Integer zoneID, String zoneName,
            Short groupID, String groupName) throws IllegalArgumentException {
        return addDefaultZoneParameter(sessionToken, zoneID, zoneName)
                .addParameter(ParameterKeys.GROUP_ID, objectToString(groupID))
                .addParameter(ParameterKeys.GROUP_NAME, groupName);
    }

    /**
     * Adds a parameter for device-requests the request-string, if the parameter value is not null.
     *
     * @param sessionToken
     * @param dsid
     * @param dSUID
     * @param name
     * @return SimpleRequestBuilder with added parameter
     * @throws IllegalArgumentException if no class and function added
     * @throws NullArgumentException if the parameterKey is null
     */
    public SimpleRequestBuilder addDefaultDeviceParameter(String sessionToken, DSID dsid, String dSUID, String name)
            throws IllegalArgumentException {
        return addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.DSID, objectToString(dsid))
                .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name);
    }

    private SimpleRequestBuilder addParameterInt(String parameterKey, String parameterValue) {
        if (allRight()) {
            if (parameterKey == null) {
                throw new IllegalArgumentException("parameterKey is null");
            }
            if (parameterValue != null) {
                if (!parameterIsAdded) {
                    parameterIsAdded = true;
                    request = request + "?" + parameterKey + "=" + parameterValue;
                } else {
                    request = request + "&" + parameterKey + "=" + parameterValue;
                }
            }
        }
        return this;
    }

    /**
     * Returns the request string.
     *
     * @return request string
     * @throws IllegalArgumentException if no class or function is added.
     */
    public String buildRequestString() throws IllegalArgumentException {
        String request = builder.buildRequestStringInt();
        LOCK.unlock();
        return request;
    }

    private String buildRequestStringInt() {
        return allRight() ? request : null;
    }

    private boolean allRight() {
        if (!classIsChosen) {
            throw new IllegalArgumentException(ExeptionConstants.NO_CLASS_ADDED);
        }
        if (!functionIsChosen) {
            throw new IllegalArgumentException(ExeptionConstants.NO_FUNCTION);
        }
        return true;
    }

    /**
     * Convert an {@link Object} to a {@link String} or null, if the obj was null or it was a negative {@link Number}.
     *
     * @param obj can be null
     * @return the {@link String} or null
     */
    public static String objectToString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof DSID) {
            return ((DSID) obj).getValue();
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue() > -1 ? obj.toString() : null;
        }
        return obj.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals()
     */
    public boolean equals(SimpleRequestBuilder builder) {
        return this.request.contains(builder.request);
    }
}
