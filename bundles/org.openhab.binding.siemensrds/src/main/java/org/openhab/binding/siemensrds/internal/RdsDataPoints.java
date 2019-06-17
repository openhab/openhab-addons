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
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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
    
    protected static final Logger LOGGER = 
            LoggerFactory.getLogger(RdsDataPoints.class);

    @SerializedName("totalCount")
    private String totalCount;
    @SerializedName("values")
    private Map<String, BasePoint> points;

    /*
     * private method:
     * execute an HTTP GET command on the remote cloud server
     */
    private static String httpGetPointListJson(String apiKey, String token, String plantId) { 
        String result = "";
        
        try {
            URL url = 
                new URL(String.format(URL_POINTS, plantId));
            
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                    
            https.setRequestMethod(HTTP_GET);
    
            https.setRequestProperty(HDR_USER_AGENT, 
                                     VAL_USER_AGENT);
    
            https.setRequestProperty(HDR_ACCEPT, 
                                     VAL_ACCEPT);
            
            https.setRequestProperty(HDR_SUB_KEY, apiKey);
    
            https.setRequestProperty(HDR_AUTHORIZE, 
                       String.format(VAL_AUTHORIZE, token));
                    
            int responseCode = https.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { 
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(https.getInputStream(), "UTF8"));
                String inStr;
                StringBuffer response = new StringBuffer();
                while ((inStr = in.readLine()) != null) {
                    response.append(inStr);
                } 
                in.close();
                result  = response.toString();
            } else {
                LOGGER.error("httpGetPointListJson: http error={}", responseCode);
            }
        } catch (Exception e) {
            LOGGER.error("httpGetPointListJson: exception={}", e.getMessage());
        }
        return result;
    }
    
    
    /*
     * public method:
     * execute a GET on the cloud server, parse the JSON, 
     * and create a real class that encapsulates the retrieved data
     */
    public static RdsDataPoints create(String apiKey, String token, String plantId) {
        RdsDataPoints result = null;

        String json = httpGetPointListJson(apiKey, token, plantId);

        try {
            if (!json.equals("")) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(BasePoint.class, 
                                new PointDeserializer())
                        .create();
                result = gson.fromJson(json, RdsDataPoints.class);
            }
        } catch (Exception e) {
            LOGGER.error("create: exception={}", e.getMessage());
        }
        
        return result;
    }


    /*
     * private method:
     * execute an HTTP PUT on the server to set a data point value
     */
    private void httpSetPointValueJson(String apiKey, String token, String pointId, String json) {  
        try {
            URL url = new URL(String.format(URL_SETVAL, pointId));
                    
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                    
            https.setRequestMethod(HTTP_PUT);
    
            https.setRequestProperty(HDR_USER_AGENT,
                                     VAL_USER_AGENT);
            
            https.setRequestProperty(HDR_ACCEPT, 
                                     VAL_ACCEPT);
            
            https.setRequestProperty(HDR_CONT_TYPE,
                                     VAL_CONT_JSON);
            
            https.setRequestProperty(HDR_AUTHORIZE, 
                       String.format(VAL_AUTHORIZE, token));
    
            https.setRequestProperty(HDR_SUB_KEY, apiKey); 

            https.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(https.getOutputStream());
            wr.writeBytes(json);
            wr.flush();
            wr.close();
            
            int responseCode = https.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.error("httpSetPointValueJson: http error={}", responseCode);
            }
        } catch (Exception e) {
            LOGGER.error("httpSetPointValueJson: exception={}", e.getMessage());
        }
    }

    
/*
 
    // ======================== CURRENTLY UN-USED CODE ======================

    // private method:
    // execute an HTTP GET on the server to refresh a single data point value
    private String httpGetPointValueJson(String token, String pointId) {  
        String result = "";

        try {
    
            URL url = 
                new URL(String.format(URL_GETVAL, pointId));
                    
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                    
            https.setRequestMethod(HTTP_GET);
    
            https.setRequestProperty(HDR_USER_AGENT,
                                     VAL_USER_AGENT);
            
            https.setRequestProperty(HDR_ACCEPT, 
                                     VAL_ACCEPT);
            
            https.setRequestProperty(HDR_AUTHORIZE, 
                    String.format(VAL_AUTHORIZE, token));
    
            https.setRequestProperty(HDR_SUB_KEY, 
                                     VAL_SUB_KEY);

            int responseCode = https.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { 
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(https.getInputStream(), "UTF8"));
                String inStr;
                StringBuffer response = new StringBuffer();
                while ((inStr = in.readLine()) != null) {
                    response.append(inStr);
                } 
                in.close();
                result = response.toString();

            } else {
                logger.error("http error: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("exception: {}", e.getMessage());
        }
        return result;
    }
    
*/
    
    /*
     * private method:
     * retrieve the data point with the given objectName
     */
    private BasePoint getPoint(String objectName) {
        if (objectName != null) {
            for (Map.Entry<String, BasePoint> entry : points.entrySet()) {
                BasePoint point = entry.getValue();
                if (point != null && point.objectName != null && 
                        point.objectName.equals(objectName)) {
                    return point;
                }
            }
        }
        return null;
    }


    /*
     * private method:
     * retrieve the data point with the given objectName
     */
    private String getPointId(String objectName) {
        if (objectName != null) {
            for (Map.Entry<String, BasePoint> entry : points.entrySet()) {
                BasePoint point = entry.getValue();
                if (point != null && point.objectName != null && 
                        point.objectName.equals(objectName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    
    /*
     * public method:
     * retrieve the state of the data point with the given objectName
     */
    public State getRaw(String objectName) {
        BasePoint point = getPoint(objectName);
        if (point != null) {
            State state = point.getRaw();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("getRaw: {} <= {}", objectName, state.toString()); 
            }
            return state;
        }
        LOGGER.error("getRaw: {} <= No Value!", objectName); 
        return null;
    }
    

    /*
     * public method:
     * retrieve the enum state of the data point with the given objectName
     */
    public State getEnum(String objectName) {
        BasePoint point = getPoint(objectName);
        if (point != null) {
            State state = point.getEnum();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("getEnum: {} <= {}", objectName, state.toString()); 
            }
            return state;
        }
        LOGGER.error("getEnum: {} <= No Value!", objectName); 
        return null;
    }
    

    /*
     * public method:
     * return the state of the "Online" data point  
     */
    public Boolean isOnline() {
        for (Map.Entry<String, BasePoint> entry : points.entrySet()) {
            BasePoint point = entry.getValue();
            if (point != null && point.memberName != null && 
                    point.memberName.equals("Online")) {
                return (point.asInt() == 1);
            }
        }
        return false;
    }


    /*
     * public method:
     * set a new data point value on the server
     */
    public void setValue(String apiKey, String token, String objectName, String value) {
        String pointId = getPointId(objectName);
        BasePoint point = getPoint(objectName);

        if (pointId != null && point != null) {
            String json = point.commandJson(value);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("setValue: {} => {}", objectName, json);
            }
            httpSetPointValueJson(apiKey, token, pointId, json);
        }
    }

    
}

    /**
     * private class:
     * a generic data point
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
         *  initialize the enum value list
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
        
        /*
         * => MUST be overridden
         */
        protected int asInt() {
            return -1;
        }

        protected boolean isEnum() {
            return (enumParsed ? isEnum : initEnum());
        }

        public State getEnum () {
            if (isEnum()) {
                int index = asInt();
                if (index >= 0 && index < enumVals.length) {
                    return new StringType(enumVals[index]);
                }
            }
            return null;
        }

        /*
         * property getter for openHAB
         * => MUST be overridden
         */
        public State getRaw () {
            return null;
        }

        /*
         * property getter for openHAB 
         * returns the "Units" of the point value
         */
        public String getUnits () {
            return (descr != null ? descr : "");
        }
        
        /*
         * property getter for JSON 
         * => MAY be overridden
         */
        public String commandJson (String newVal) {
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
     * private class 
     * a data point where "value" is a JSON text element
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
            }
            catch (Exception e) {
                return -1;
            }
        }

        @Override
        /*
         * if appropriate return the enum string representation, 
         * otherwise return the decimal representation
         */
        public State getRaw () {
            return new StringType(value);
        }
    }
    
    
    /**
     * private class 
     * a data point where "value" is a JSON boolean element
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
        public State getRaw () {
            return OnOffType.from(value);
        }
    }
    
    
    /**
     * private class 
     * inner (helper) class for an embedded JSON numeric element
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
     * private class 
     * a data point where "value" is a nested JSON numeric element
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

        @Override
        /*
         * if appropriate return the enum string representation, 
         * otherwise return the decimal representation
         */
        public State getRaw () {
            if (inner != null) {
                return new DecimalType(inner.value);
            }
            return null;
        }
    }
    
    
    /**
     * private class 
     * a data point where "value" is a JSON numeric element
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

        @Override
        /*
         * if appropriate return the enum string representation, 
         * otherwise return the decimal representation
         */
        public State getRaw () {
            return new DecimalType(value);
        }
    }
    

    /**
     * private class 
     * a JSON de-serializer for the Data Point classes above
     *
     * @author Andrew Fiddian-Green - Initial contribution
     * 
     */
    class PointDeserializer implements JsonDeserializer<BasePoint> {
    
        @Override
        public BasePoint deserialize(JsonElement element, Type guff, 
                                 JsonDeserializationContext ctxt) 
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
                            return ctxt.deserialize(obj, 
                                    InnerValuePoint.class);
                        } else {
                            if (rep.isJsonPrimitive() && 
                                    rep.getAsJsonPrimitive().isNumber()) {
                                switch (rep.getAsInt()) {
                                    case 1:
                                    case 3: 
                                        return ctxt.deserialize(obj, 
                                                InnerValuePoint.class);
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
