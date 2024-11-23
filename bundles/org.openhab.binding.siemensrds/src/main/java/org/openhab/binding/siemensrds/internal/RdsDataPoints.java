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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemensrds.points.BasePoint;
import org.openhab.binding.siemensrds.points.PointDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 *
 * Interface to the Data Points of a particular Plant
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class RdsDataPoints {

    /*
     * NOTE: requires a static logger because the class has static methods
     */
    protected final Logger logger = LoggerFactory.getLogger(RdsDataPoints.class);

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(BasePoint.class, new PointDeserializer())
            .create();

    /*
     * this is a second index into to the JSON "values" points Map below; the
     * purpose is to allow point lookups by a) pointId (which we do directly from
     * the Map, and b) by pointClass (which we do indirectly "double dereferenced"
     * via this index
     */
    private final Map<String, String> indexClassToId = new HashMap<>();

    @SerializedName("totalCount")
    private @Nullable String totalCount;
    @SerializedName("values")
    public @Nullable Map<String, @Nullable BasePoint> points;

    private String valueFilter = "";

    /*
     * protected static method: can be used by this class and by other classes to
     * execute an HTTP GET command on the remote cloud server to retrieve the JSON
     * response from the given urlString
     */
    protected static String httpGenericGetJson(String apiKey, String token, String urlString) throws IOException {
        /*
         * NOTE: this class uses JAVAX HttpsURLConnection library instead of the
         * preferred JETTY library; the reason is that JETTY does not allow sending the
         * square brackets characters "[]" verbatim over HTTP connections
         */
        URL url = new URL(urlString);
        HttpsURLConnection https = (HttpsURLConnection) url.openConnection();

        https.setRequestMethod(HTTP_GET);

        https.setRequestProperty(USER_AGENT, MOZILLA);
        https.setRequestProperty(ACCEPT, APPLICATION_JSON);
        https.setRequestProperty(SUBSCRIPTION_KEY, apiKey);
        https.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        if (https.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(https.getResponseMessage());
        }

        try (InputStream inputStream = https.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String inputString;
            StringBuffer response = new StringBuffer();
            while ((inputString = reader.readLine()) != null) {
                response.append(inputString);
            }
            return response.toString();
        }
    }

    /*
     * public static method: parse the JSON, and create a real instance of this
     * class that encapsulates the data data point values
     */
    public static @Nullable RdsDataPoints createFromJson(String json) {
        return GSON.fromJson(json, RdsDataPoints.class);
    }

    /*
     * private method: execute an HTTP PUT on the server to set a data point value
     */
    private void httpSetPointValueJson(String apiKey, String token, String pointUrl, String json)
            throws RdsCloudException, ProtocolException, MalformedURLException, IOException {
        /*
         * NOTE: this class uses JAVAX HttpsURLConnection library instead of the
         * preferred JETTY library; the reason is that JETTY does not allow sending the
         * square brackets characters "[]" verbatim over HTTP connections
         */
        URL url = new URL(pointUrl);

        HttpsURLConnection https = (HttpsURLConnection) url.openConnection();

        https.setRequestMethod(HTTP_PUT);

        https.setRequestProperty(USER_AGENT, MOZILLA);
        https.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
        https.setRequestProperty(SUBSCRIPTION_KEY, apiKey);
        https.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        https.setDoOutput(true);

        try (OutputStream outputStream = https.getOutputStream();
                DataOutputStream writer = new DataOutputStream(outputStream)) {
            writer.writeBytes(json);
        }

        if (https.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(https.getResponseMessage());
        }
    }

    /*
     * public method: retrieve the data point with the given pointClass
     */
    public BasePoint getPointByClass(String pointClass) throws RdsCloudException {
        if (indexClassToId.isEmpty()) {
            initClassToIdNameIndex();
        }
        @Nullable
        String pointId = indexClassToId.get(pointClass);
        if (pointId != null) {
            return getPointById(pointId);
        }
        throw new RdsCloudException(String.format("pointClass \"%s\" not found", pointClass));
    }

    /*
     * public method: retrieve the data point with the given pointId
     */
    public BasePoint getPointById(String pointId) throws RdsCloudException {
        Map<String, @Nullable BasePoint> points = this.points;
        if (points != null) {
            @Nullable
            BasePoint point = points.get(pointId);
            if (point != null) {
                return point;
            }
        }
        throw new RdsCloudException(String.format("pointId \"%s\" not found", pointId));
    }

    /*
     * private method: retrieve Id of data point with the given pointClass
     */
    public String pointClassToId(String pointClass) throws RdsCloudException {
        if (indexClassToId.isEmpty()) {
            initClassToIdNameIndex();
        }
        @Nullable
        String pointId = indexClassToId.get(pointClass);
        if (pointId != null && !pointId.isEmpty()) {
            return pointId;
        }
        throw new RdsCloudException(String.format("no pointId to match pointClass \"%s\"", pointClass));
    }

    /*
     * public method: return the state of the "Online" data point
     */
    public boolean isOnline() throws RdsCloudException {
        BasePoint point = getPointByClass(HIE_ONLINE);
        return "Online".equals(point.getEnum().toString());
    }

    /*
     * public method: set a new data point value on the server
     */
    public void setValue(String apiKey, String token, String pointClass, String value) {
        try {
            String pointId = pointClassToId(pointClass);
            BasePoint point = getPointByClass(pointClass);

            String url = String.format(URL_SETVAL, pointId);
            String payload = point.commandJson(value);

            if (logger.isTraceEnabled()) {
                logger.trace(LOG_HTTP_COMMAND, HTTP_PUT, url.length());
                logger.trace(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, url);
                logger.trace(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, payload);
            } else if (logger.isDebugEnabled()) {
                logger.debug(LOG_HTTP_COMMAND_ABR, HTTP_PUT, url.length());
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_SENDING_MARK, url.substring(0, Math.min(url.length(), 30)));
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_SENDING_MARK,
                        payload.substring(0, Math.min(payload.length(), 30)));
            }

            httpSetPointValueJson(apiKey, token, url, payload);
        } catch (RdsCloudException e) {
            logger.warn(LOG_SYSTEM_EXCEPTION, "setValue()", e.getClass().getName(), e.getMessage());
        } catch (JsonParseException | IOException e) {
            logger.warn(LOG_RUNTIME_EXCEPTION, "setValue()", e.getClass().getName(), e.getMessage());
        }
    }

    /*
     * public method: refresh the data point value from the server
     */
    public boolean refresh(String apiKey, String token) {
        try {
            // initialize the value filter
            if (valueFilter.isEmpty()) {
                Set<String> set = new HashSet<>();
                String pointId;

                for (ChannelMap channel : CHAN_MAP) {
                    try {
                        pointId = pointClassToId(channel.clazz);
                        set.add(String.format("\"%s\"", pointId));
                    } catch (RdsCloudException e) {
                        logger.debug("{} \"{}\" not implemented; don't include in request", channel.id, channel.clazz);
                    }
                }

                Map<String, @Nullable BasePoint> points = this.points;
                if (points != null) {
                    for (Map.Entry<String, @Nullable BasePoint> entry : points.entrySet()) {
                        @Nullable
                        BasePoint point = entry.getValue();
                        if (point != null) {
                            if ("Online".equals(point.getMemberName())) {
                                set.add(String.format("\"%s\"", entry.getKey()));
                                break;
                            }
                        }
                    }
                }

                valueFilter = String.join(",", set);
            }

            String url = String.format(URL_VALUES, valueFilter);

            if (logger.isTraceEnabled()) {
                logger.trace(LOG_HTTP_COMMAND, HTTP_GET, url.length());
                logger.trace(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, url);
            } else if (logger.isDebugEnabled()) {
                logger.debug(LOG_HTTP_COMMAND_ABR, HTTP_GET, url.length());
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_SENDING_MARK, url.substring(0, Math.min(url.length(), 30)));
            }

            String json = httpGenericGetJson(apiKey, token, url);

            if (logger.isTraceEnabled()) {
                logger.trace(LOG_CONTENT_LENGTH, LOG_RECEIVED_MSG, json.length());
                logger.trace(LOG_PAYLOAD_FMT, LOG_RECEIVED_MARK, json);
            } else if (logger.isDebugEnabled()) {
                logger.debug(LOG_CONTENT_LENGTH_ABR, LOG_RECEIVED_MSG, json.length());
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_RECEIVED_MARK, json.substring(0, Math.min(json.length(), 30)));
            }

            @Nullable
            RdsDataPoints newPoints = GSON.fromJson(json, RdsDataPoints.class);

            Map<String, @Nullable BasePoint> newPointsMap = newPoints != null ? newPoints.points : null;

            if (newPointsMap == null) {
                throw new RdsCloudException("new points map empty");
            }

            synchronized (this) {
                for (Entry<String, @Nullable BasePoint> entry : newPointsMap.entrySet()) {
                    @Nullable
                    String pointId = entry.getKey();

                    @Nullable
                    BasePoint newPoint = entry.getValue();
                    if (newPoint == null) {
                        throw new RdsCloudException("invalid new point");
                    }

                    @Nullable
                    BasePoint myPoint = getPointById(pointId);

                    if (!(newPoint.getClass().equals(myPoint.getClass()))) {
                        throw new RdsCloudException("existing vs. new point class mismatch");
                    }

                    myPoint.refreshValueFrom(newPoint);

                    if (logger.isDebugEnabled()) {
                        logger.debug("refresh {}.{}: {} << {}", getDescription(), myPoint.getPointClass(),
                                myPoint.getState(), newPoint.getState());
                    }
                }
            }

            return true;
        } catch (RdsCloudException e) {
            logger.warn(LOG_SYSTEM_EXCEPTION, "refresh()", e.getClass().getName(), e.getMessage());
        } catch (JsonParseException | IOException e) {
            logger.warn(LOG_RUNTIME_EXCEPTION, "refresh()", e.getClass().getName(), e.getMessage());
        }
        return false;
    }

    /*
     * initialize the second index into to the points Map
     */
    private void initClassToIdNameIndex() {
        Map<String, @Nullable BasePoint> points = this.points;
        if (points != null) {
            indexClassToId.clear();
            for (Entry<String, @Nullable BasePoint> entry : points.entrySet()) {
                String pointKey = entry.getKey();
                BasePoint pointValue = entry.getValue();
                if (pointValue != null) {
                    indexClassToId.put(pointValue.getPointClass(), pointKey);
                }
            }
        }
    }

    /*
     * public method: return the state of the "Description" data point
     */
    public String getDescription() throws RdsCloudException {
        return getPointByClass(HIE_DESCRIPTION).getState().toString();
    }
}
