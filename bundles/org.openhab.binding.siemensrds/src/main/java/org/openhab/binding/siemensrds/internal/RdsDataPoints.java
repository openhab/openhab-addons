/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * Interface to the Data Points of a particular Plant
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class RdsDataPoints {

    /*
     * NOTE: requires a static logger because the class has static methods
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(RdsDataPoints.class);

    @SerializedName("totalCount")
    private String totalCount;
    @SerializedName("values")
    private Map<String, BasePoint> points;

    private String valueFilter = "";

    /*
     * protected static method: can be used by this class and by other classes to
     * execute an HTTP GET command on the remote cloud server to retrieve the JSON
     * response from the given urlString
     */
    protected static String httpGenericGetJson(String apiKey, String token, String urlString) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LOGGER.error("httpGenericGetJson: invalid url {}", urlString);
            return "";
        }

        /*
         * NOTE: this class uses JAVAX HttpsURLConnection library instead of the
         * preferred JETTY library; the reason is that JETTY does not allow sending the
         * square brackets characters "[]" verbatim over HTTP connections
         */
        HttpsURLConnection https;
        try {
            https = (HttpsURLConnection) url.openConnection();
        } catch (java.io.IOException e) {
            LOGGER.error("httpGenericGetJson: unable to connect to Cloud Server");
            return "";
        }

        try {
            https.setRequestMethod(HTTP_GET);
        } catch (ProtocolException e) {
            // we shouldn't ever reach here because HTTP GET is a valid method
            return "";
        }

        https.setRequestProperty(USER_AGENT, MOZILLA);
        https.setRequestProperty(ACCEPT, APPLICATION_JSON);
        https.setRequestProperty(SUBSCRIPTION_KEY, apiKey);
        https.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        int responseCode;
        try {
            responseCode = https.getResponseCode();
        } catch (IOException e) {
            LOGGER.error("httpGenericGetJson: missing HTTP response from Cloud Server");
            return "";
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            LOGGER.error("httpGenericGetJson: invalid HTTP response {} from Cloud Server", responseCode);
            return "";
        }

        try (InputStream inputStream = https.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF8");) {

            String inputString;
            StringBuffer response = new StringBuffer();
            BufferedReader reader = new BufferedReader(inputStreamReader);
            while ((inputString = reader.readLine()) != null) {
                response.append(inputString);
            }

            return response.toString();

        } catch (UnsupportedEncodingException e) {
            // we shouldn't ever reach here because UTF8 is a valid encoding
            return "";
        } catch (IOException e) {
            LOGGER.error("httpGenericGetJson: unable to read response from Cloud Server");
            return "";
        }
    }

    /*
     * private method: execute an HTTP GET command on the remote cloud server to
     * retrieve the full data point list for the given plantId
     */
    private static String httpGetPointListJson(String apiKey, String token, String plantId) {
        return httpGenericGetJson(apiKey, token, String.format(URL_POINTS, plantId));
    }

    /*
     * public static method: execute a GET on the cloud server, parse the JSON, and
     * create a real instance of this class that encapsulates all the retrieved data
     * point values
     */
    @Nullable
    public static RdsDataPoints create(String apiKey, String token, String plantId) {
        String json = httpGetPointListJson(apiKey, token, plantId);

        LOGGER.debug("create: received {} characters (log:set TRACE to see all)", json.length());

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("create: response={}", json);

        try {
            if (!json.equals("")) {
                Gson gson = new GsonBuilder().registerTypeAdapter(BasePoint.class, new PointDeserializer()).create();
                return gson.fromJson(json, RdsDataPoints.class);
            }
        } catch (Exception e) {
            LOGGER.error("create: exception={}", e.getMessage());
        }
        return null;
    }

    /*
     * private method: execute an HTTP PUT on the server to set a data point value
     */
    private void httpSetPointValueJson(String apiKey, String token, String pointId, String json) {
        URL url;
        String urlString = String.format(URL_SETVAL, pointId);
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LOGGER.error("httpSetPointValueJson: invalid url {}", urlString);
            return;
        }

        /*
         * NOTE: this class uses JAVAX HttpsURLConnection library instead of the
         * preferred JETTY library; the reason is that JETTY does not allow sending the
         * square brackets characters "[]" verbatim over HTTP connections
         */
        HttpsURLConnection https;
        try {
            https = (HttpsURLConnection) url.openConnection();
        } catch (java.io.IOException e) {
            LOGGER.error("httpSetPointValueJson: unable to connect to Cloud Server");
            return;
        }

        try {
            https.setRequestMethod(HTTP_PUT);
        } catch (ProtocolException e) {
            // we shouldn't ever reach here because HTTP PUT is a valid method
            return;
        }

        https.setRequestProperty(USER_AGENT, MOZILLA);
        https.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
        https.setRequestProperty(SUBSCRIPTION_KEY, apiKey);
        https.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        https.setDoOutput(true);

        try (OutputStream outputStream = https.getOutputStream();
                DataOutputStream writer = new DataOutputStream(outputStream);) {
            writer.writeBytes(json);
        } catch (IOException e) {
            LOGGER.error("httpSetPointValueJson: error sending request to Cloud Server");
            return;
        }

        int responseCode;
        try {
            responseCode = https.getResponseCode();
        } catch (IOException e) {
            LOGGER.error("httpGetPlantListJson: missing HTTP response from Cloud Server");
            return;
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            LOGGER.error("httpGetPlantListJson: invalid HTTP response {} from Cloud Server", responseCode);
            return;
        }
        return;
    }

    /*
     * private method: execute an HTTP GET command on the remote cloud server to
     * retrieve the data points given in filterId
     */
    private String httpGetPointValuesJson(String apiKey, String token, String filterId) {
        return httpGenericGetJson(apiKey, token, String.format(URL_VALUES, filterId));
    }

    /*
     * private method: retrieve the data point with the given hierarchyName
     */
    private BasePoint getPoint(String hierarchyName) {
        if (hierarchyName != null) {
            for (Map.Entry<String, BasePoint> entry : points.entrySet()) {
                BasePoint point = entry.getValue();
                if (point != null && point.hierarchyName != null && point.hierarchyName.contains(hierarchyName)) {
                    return point;
                }
            }
        }
        return null;
    }

    /*
     * private method: retrieve the data point with the given hierarchyName
     */
    private String getPointId(String hierarchyName) {
        if (hierarchyName != null) {
            for (Map.Entry<String, BasePoint> entry : points.entrySet()) {
                BasePoint point = entry.getValue();
                if (point != null && point.hierarchyName != null && point.hierarchyName.contains(hierarchyName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /*
     * public method: retrieve the state of the data point with the given
     * hierarchyName
     */
    public synchronized State getRaw(String hierarchyName) {
        BasePoint point = getPoint(hierarchyName);
        if (point != null) {
            State state = point.getRaw();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getRaw: {}={}", hierarchyName, state.toString());
            }
            return state;
        }
        LOGGER.error("getRaw: {}=No Value!", hierarchyName);
        return null;
    }

    /*
     * public method: return the presentPriority of the data point with the given
     * hierarchyName
     */
    public synchronized int getPresPrio(String hierarchyName) {
        BasePoint point = getPoint(hierarchyName);
        if (point != null) {
            int presentPriority = point.getPresentPriority();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getPresentPriority: {}={}", hierarchyName, presentPriority);
            }
            return presentPriority;
        }
        LOGGER.error("getPresentPriority: {}=No Value!", hierarchyName);
        return 0;
    }

    /*
     * public method: return the presentPriority of the data point with the given
     * hierarchyName
     */
    public synchronized int asInt(String hierarchyName) {
        BasePoint point = getPoint(hierarchyName);
        if (point != null) {
            int value = point.asInt();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("asInt: {}={}", hierarchyName, value);
            }
            return value;
        }
        LOGGER.error("getAsInt: {}=No Value!", hierarchyName);
        return 0;
    }

    /*
     * public method: retrieve the enum state of the data point with the given
     * hierarchyName
     */
    public synchronized State getEnum(String hierarchyName) {
        BasePoint point = getPoint(hierarchyName);
        if (point != null) {
            State state = point.getEnum();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getEnum: {}={}", hierarchyName, state.toString());
            }
            return state;
        }
        LOGGER.error("getEnum: {}=No Value!", hierarchyName);
        return null;
    }

    /*
     * public method: return the state of the "Online" data point
     */
    public Boolean isOnline() {
        for (Map.Entry<String, BasePoint> entry : points.entrySet()) {
            BasePoint point = entry.getValue();
            if (point != null && point.memberName != null && point.memberName.equals("Online")) {
                return (point.asInt() == 1);
            }
        }
        return false;
    }

    /*
     * public method: set a new data point value on the server
     */
    public void setValue(String apiKey, String token, String hierarchyName, String value) {
        String pointId = getPointId(hierarchyName);
        BasePoint point = getPoint(hierarchyName);

        if (pointId != null && point != null) {
            String json = point.commandJson(value);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("setValue: {}=>{}", hierarchyName, json);
            }
            httpSetPointValueJson(apiKey, token, pointId, json);
        }
    }

    /*
     * public method: set a new data point value on the server
     */
    public boolean refresh(String apiKey, String token) {
        @Nullable
        RdsDataPoints nuPoints = null;

        if (valueFilter.equals("")) {
            Set<String> set = new HashSet<>();
            String pointId;

            for (ChannelMap chan : CHAN_MAP) {
                pointId = getPointId(chan.hierarchyName);
                if (pointId != null) {
                    set.add(String.format("\"%s\"", pointId));
                }
            }
            valueFilter = String.join(",", set);
        }

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("refresh: request={}", valueFilter);

        String json = httpGetPointValuesJson(apiKey, token, valueFilter);

        LOGGER.debug("refresh: received {} characters (log:set TRACE to see all)", json.length());

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("refresh: response={}", json);

        if (json.equals("")) {
            return false;
        }

        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(BasePoint.class, new PointDeserializer()).create();
            nuPoints = gson.fromJson(json, RdsDataPoints.class);
        } catch (Exception e) {
            LOGGER.error("refreshUsedValues: exception={}", e.getMessage());
            return false;
        }

        synchronized (this) {
            for (Map.Entry<String, BasePoint> entry : nuPoints.points.entrySet()) {
                BasePoint nuPoint = entry.getValue();
                BasePoint exPoint = points.get(entry.getKey());

                if (nuPoint instanceof BooleanPoint && exPoint instanceof BooleanPoint) {
                    ((BooleanPoint) exPoint).value = ((BooleanPoint) nuPoint).value;
                } else

                if (nuPoint instanceof TextPoint && exPoint instanceof TextPoint) {
                    ((TextPoint) exPoint).value = ((TextPoint) nuPoint).value;
                } else

                if (nuPoint instanceof NumericPoint && exPoint instanceof NumericPoint) {
                    ((NumericPoint) exPoint).value = ((NumericPoint) nuPoint).value;
                } else

                if (nuPoint instanceof InnerValuePoint && exPoint instanceof InnerValuePoint) {
                    ((InnerValuePoint) exPoint).inner.value = ((InnerValuePoint) nuPoint).inner.value;
                    ((InnerValuePoint) exPoint).inner.presentPriority = ((InnerValuePoint) nuPoint).inner.presentPriority;
                }
            }
        }
        return true;
    }

}

/**
 * private class: a generic data point
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class BasePoint {
    @SerializedName("rep")
    protected int rep;
    @SerializedName("type")
    protected int type;
    @SerializedName("write")
    protected boolean write;
    @SerializedName("descr")
    protected String descr;
    @SerializedName("limits")
    protected float[] limits;
    @SerializedName("descriptionName")
    protected String descriptionName;
    @SerializedName("objectName")
    protected String objectName;
    @SerializedName("memberName")
    protected String memberName;
    @SerializedName("hierarchyName")
    protected String hierarchyName;
    @SerializedName("translated")
    protected boolean translated;

    private String[] enumVals;
    private boolean enumParsed = false;
    protected boolean isEnum = false;

    /*
     * initialize the enum value list
     */
    private boolean initEnum() {
        if (!enumParsed) {
            if (descr != null && descr.contains("*")) {
                enumVals = descr.split("\\*");
                isEnum = true;
            }
        }
        enumParsed = true;
        return isEnum;
    }

    public int getPresentPriority() {
        return 0;
    }

    /*
     * => MUST be overridden
     */
    protected int asInt() {
        return -1;
    }

    protected boolean isEnum() {
        return (enumParsed ? isEnum : initEnum());
    }

    public State getEnum() {
        if (isEnum()) {
            int index = asInt();
            if (index >= 0 && index < enumVals.length) {
                return new StringType(enumVals[index]);
            }
        }
        return null;
    }

    /*
     * property getter for openHAB => MUST be overridden
     */
    public State getRaw() {
        return null;
    }

    /*
     * property getter for openHAB returns the "Units" of the point value
     */
    public String getUnits() {
        return (descr != null ? descr : "");
    }

    /*
     * property getter for JSON => MAY be overridden
     */
    public String commandJson(String newVal) {
        if (isEnum()) {
            for (int index = 0; index < enumVals.length; index++) {
                if (enumVals[index].equals(newVal)) {
                    return String.format("{\"value\":%d}", index);
                }
            }
        }
        return String.format("{\"value\":%s}", newVal);
    }

}

/**
 * private class a data point where "value" is a JSON text element
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class TextPoint extends BasePoint {
    @SerializedName("value")
    protected String value;

    @Override
    protected int asInt() {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    /*
     * if appropriate return the enum string representation, otherwise return the
     * decimal representation
     */
    @Override
    public State getRaw() {
        return new StringType(value);
    }
}

/**
 * private class a data point where "value" is a JSON boolean element
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class BooleanPoint extends BasePoint {
    @SerializedName("value")
    protected boolean value;

    @Override
    protected int asInt() {
        return (value ? 1 : 0);
    }

    @Override
    public State getRaw() {
        return OnOffType.from(value);
    }
}

/**
 * private class inner (helper) class for an embedded JSON numeric element
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class NestedValue {
    @SerializedName("value")
    protected float value;
    @SerializedName("presentPriority")
    protected float presentPriority;
}

/**
 * private class a data point where "value" is a nested JSON numeric element
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class InnerValuePoint extends BasePoint {
    @SerializedName("value")
    protected NestedValue inner;

    @Override
    protected int asInt() {
        return (inner != null ? (int) inner.value : -1);
    }

    /*
     * if appropriate return the enum string representation, otherwise return the
     * decimal representation
     */
    @Override
    public State getRaw() {
        if (inner != null) {
            return new DecimalType(inner.value);
        }
        return null;
    }

    @Override
    public int getPresentPriority() {
        if (inner != null) {
            return (int) inner.presentPriority;
        }
        return 0;
    }
}

/**
 * private class a data point where "value" is a JSON numeric element
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class NumericPoint extends BasePoint {
    @SerializedName("value")
    protected float value;

    @Override
    protected int asInt() {
        return (int) value;
    }

    /*
     * if appropriate return the enum string representation, otherwise return the
     * decimal representation
     */
    @Override
    public State getRaw() {
        return new DecimalType(value);
    }
}

/**
 * private class a JSON de-serializer for the Data Point classes above
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class PointDeserializer implements JsonDeserializer<BasePoint> {

    @Override
    public BasePoint deserialize(JsonElement element, Type guff, JsonDeserializationContext ctxt)
            throws JsonParseException {
        JsonObject obj = element.getAsJsonObject();
        JsonElement val = obj.get("value");

        if (val != null) {
            if (val.isJsonPrimitive()) {
                if (val.getAsJsonPrimitive().isBoolean()) {
                    return ctxt.deserialize(obj, BooleanPoint.class);
                }
                if (val.getAsJsonPrimitive().isNumber()) {
                    return ctxt.deserialize(obj, NumericPoint.class);
                }
                if (val.getAsJsonPrimitive().isString()) {
                    return ctxt.deserialize(obj, TextPoint.class);
                }
            } else {
                if (val.isJsonObject()) {
                    JsonElement rep = obj.get("rep");
                    if (rep == null) {
                        return ctxt.deserialize(obj, InnerValuePoint.class);
                    } else {
                        if (rep.isJsonPrimitive() && rep.getAsJsonPrimitive().isNumber()) {
                            switch (rep.getAsInt()) {
                            case 1:
                            case 3:
                                return ctxt.deserialize(obj, InnerValuePoint.class);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
