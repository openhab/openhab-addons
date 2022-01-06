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
package org.openhab.binding.siemensrds.points;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * private class a JSON de-serializer for the Data Point classes above
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PointDeserializer implements JsonDeserializer<BasePoint> {

    private static enum PointType {
        UNDEFINED,
        STRING,
        NESTED_NUMBER,
        NUMBER
    }

    @Override
    public @Nullable BasePoint deserialize(JsonElement element, Type guff, JsonDeserializationContext ctxt)
            throws JsonParseException {
        JsonObject obj = element.getAsJsonObject();
        JsonElement value = obj.get("value");
        if (value == null) {
            UndefPoint point = ctxt.deserialize(obj, UndefPoint.class);
            if (point != null) {
                return point;
            }
            throw new JsonSyntaxException("unable to parse point WITHOUT a \"value\" element");
        }

        PointType pointType = PointType.UNDEFINED;

        boolean valueIsPrimitive = value.isJsonPrimitive();

        JsonElement rep = obj.get("rep");
        if (rep != null && rep.isJsonPrimitive() && rep.getAsJsonPrimitive().isNumber()) {
            /*
             * full point lists have a "rep" element so we know explicitly the point class
             */
            int repValue = rep.getAsInt();
            if (repValue == 0) {
                pointType = PointType.STRING;
            } else if (repValue < 4) {
                pointType = valueIsPrimitive ? PointType.NUMBER : PointType.NESTED_NUMBER;
            }
        } else {
            /*
             * refresh point lists do NOT have a "rep" element so try to infer the point
             * class
             */
            if (valueIsPrimitive) {
                JsonPrimitive primitiveType = value.getAsJsonPrimitive();
                pointType = primitiveType.isString() ? PointType.STRING : PointType.NUMBER;
            } else {
                pointType = PointType.NESTED_NUMBER;
            }
        }

        BasePoint point;
        switch (pointType) {
            case STRING: {
                point = ctxt.deserialize(obj, StringPoint.class);
                if (point != null) {
                    return point;
                }
                break;
            }
            case NESTED_NUMBER: {
                point = ctxt.deserialize(obj, NestedNumberPoint.class);
                if (point != null) {
                    return point;
                }
                break;
            }
            case NUMBER: {
                point = ctxt.deserialize(obj, NumberPoint.class);
                if (point != null) {
                    return point;
                }
                break;
            }
            default: {
                point = ctxt.deserialize(obj, UndefPoint.class);
                if (point != null) {
                    return point;
                }
            }
        }
        throw new JsonSyntaxException("unable to parse point with a \"value\" element");
    }
}
