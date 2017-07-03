/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.scalarweb.ScalarUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebResult.
 *
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebResult {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebResult.class);

    /** The id. */
    private final int id;

    /** The Constant IllegalArgument. */
    public static final int IllegalArgument = 3;

    /** The Constant IllegalState. */
    public static final int IllegalState = 7; // such as pip status when not in pip

    /** The Constant NotImplemented. */
    public static final int NotImplemented = 12;

    /** The Constant DisplayIsOff. */
    public static final int DisplayIsOff = 40005;

    /** The Constant FailedToLauch. */
    public static final int FailedToLauch = 41401;

    /** The results. */
    @SerializedName(value = "result", alternate = { "results" })
    private final JsonArray results;

    /** The errors. */
    @SerializedName(value = "error", alternate = { "errors" })
    private final JsonArray errors;

    /**
     * Instantiates a new scalar web result.
     *
     * @param id the id
     * @param results the results
     * @param errors the errors
     */
    public ScalarWebResult(int id, JsonArray results, JsonArray errors) {
        logger.debug(">> in const: {}, {}, {}", id, results, errors);
        this.id = id;
        this.results = results;
        this.errors = errors;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the results.
     *
     * @return the results
     */
    public JsonArray getResults() {
        return results;
    }

    /**
     * Checks for results.
     *
     * @return true, if successful
     */
    public boolean hasResults() {
        return !isEmpty(results);
    }

    /**
     * Checks if is empty.
     *
     * @param arry the arry
     * @return true, if is empty
     */
    private boolean isEmpty(JsonArray arry) {
        if (arry == null || arry.size() == 0) {
            return true;
        }
        for (JsonElement elm : arry) {
            if (elm.isJsonArray()) {
                if (!isEmpty(elm.getAsJsonArray())) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if is error.
     *
     * @return true, if is error
     */
    public boolean isError() {
        return errors != null && errors.size() > 0;
    }

    /**
     * Gets the http response.
     *
     * @return the http response
     */
    public HttpResponse getHttpResponse() {
        if (errors == null || errors.size() == 0) {
            return new HttpResponse(HttpStatus.SC_OK, "OK");
        } else if (errors.size() == 1) {
            return new HttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, errors.get(0).getAsString());
        } else {
            final int httpCode = errors.get(0).getAsInt();
            final String reason = errors.get(1).getAsString();
            return new HttpResponse(httpCode, reason);
        }
    }

    /**
     * As.
     *
     * @param <T> the generic type
     * @param clazz the clazz
     * @return the t
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public <T> T as(Class<T> clazz) throws IOException {
        // logger.debug(">>> as: {}", clazz);
        if (isError()) {
            throw getHttpResponse().createException();
        }

        try {
            Constructor<T> constr = clazz.getConstructor(ScalarWebResult.class);
            return constr.newInstance(this);
        } catch (NoSuchMethodException e) {
            final JsonArray results = getResults();
            if (results.size() == 0) {
                return null;
            } else if (results.size() == 1) {
                JsonElement elm = results.get(0);
                if (elm.isJsonArray()) {
                    final JsonArray arry = elm.getAsJsonArray();
                    if (arry.size() == 1) {
                        elm = arry.get(0);
                    } else {
                        elm = arry;
                    }
                }
                final Gson gson = new Gson();

                if (elm.isJsonObject()) {
                    final JsonObject jobj = elm.getAsJsonObject();
                    jobj.add("fieldNames", ScalarUtilities.getFields(jobj));
                    return gson.fromJson(jobj, clazz);
                } else {
                    return gson.fromJson(elm, clazz);
                }
            }
            // logger.debug(">>> Couldn't do it", e);
            throw new IllegalArgumentException(
                    "Constructor with ScalarWebResult argument can't be called: " + e.getMessage() + " for " + clazz,
                    e);

        } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // logger.debug(">>> Couldn't do it thrown", e);
            throw new IllegalArgumentException(
                    "Constructor with ScalarWebResult argument can't be called: " + e.getMessage(), e);
        }
    }

    /**
     * As array.
     *
     * @param <T> the generic type
     * @param clazz the clazz
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public <T> List<T> asArray(Class<T> clazz) throws IOException {
        if (isError()) {
            throw getHttpResponse().createException();
        }

        Logger logger = LoggerFactory.getLogger(ScalarWebResult.class);

        int count = results == null ? -1 : results.size();
        logger.debug(">>> results: {}: {}", id, count);

        final Gson gson = new Gson();
        final List<T> rc = new ArrayList<T>();
        final JsonArray rslts = getResults();

        if (rslts != null) {
            for (JsonElement resElm : rslts) {
                if (resElm.isJsonArray()) {
                    for (JsonElement elm : resElm.getAsJsonArray()) {

                        if (elm.isJsonObject()) {
                            final JsonObject jobj = elm.getAsJsonObject();
                            jobj.add("fieldNames", ScalarUtilities.getFields(jobj));
                            rc.add(gson.fromJson(jobj, clazz));
                        } else {
                            rc.add(gson.fromJson(elm, clazz));
                        }
                    }
                } else {

                    if (resElm.isJsonObject()) {
                        final JsonObject jobj = resElm.getAsJsonObject();
                        jobj.add("fieldNames", ScalarUtilities.getFields(jobj));
                        rc.add(gson.fromJson(jobj, clazz));
                    } else {
                        rc.add(gson.fromJson(resElm, clazz));
                    }
                }

            }
        }
        return rc;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("id: ");
        sb.append(id);

        if (errors != null && errors.size() > 0) {
            sb.append(", Error: ");
            sb.append(errors.toString());
        } else if (results != null) {
            sb.append(", Results: ");
            sb.append(results.toString());
        }

        return sb.toString();

    }
    // not sure if needed
    // public static ScalarWebResult Parse(String jsonResult) {
    // final JsonParser parser = new JsonParser();
    // final JsonElement json = parser.parse(jsonResult);
    //
    // if (!json.isJsonObject()) {
    // throw new JsonParseException("JsonResult isn't an object " + jsonResult);
    // }
    //
    // final JsonObject jsonObj = json.getAsJsonObject();
    //
    // final JsonElement idElm = jsonObj.get("id");
    // if (idElm == null) {
    // throw new JsonParseException("Result has no id memeber: " + jsonResult);
    // }
    //
    // int id;
    // try {
    // id = idElm.getAsInt();
    // } catch (NumberFormatException e) {
    // throw new JsonParseException("ID member isn't an integer: " + idElm);
    // }
    //
    // final JsonElement results = jsonObj.get(jsonObj.has("result") ? "result" : "results");
    // if (results == null) {
    // throw new JsonParseException("Result has no result/results memeber: " + jsonResult);
    // }
    //
    // if (!results.isJsonArray()) {
    // throw new JsonParseException("result/results memeber wasn't an array: " + results);
    // }
    // return new ScalarWebResult(id, results.getAsJsonArray());
    // }
}
