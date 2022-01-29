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
package org.openhab.transform.bin2json.internal;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;

import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.exceptions.JBBPException;
import com.igormaznitsa.jbbp.model.JBBPAbstractArrayField;
import com.igormaznitsa.jbbp.model.JBBPAbstractField;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayBit;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayBoolean;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayByte;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayInt;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayLong;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayShort;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayStruct;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayUByte;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayUShort;
import com.igormaznitsa.jbbp.model.JBBPFieldBit;
import com.igormaznitsa.jbbp.model.JBBPFieldBoolean;
import com.igormaznitsa.jbbp.model.JBBPFieldByte;
import com.igormaznitsa.jbbp.model.JBBPFieldInt;
import com.igormaznitsa.jbbp.model.JBBPFieldLong;
import com.igormaznitsa.jbbp.model.JBBPFieldShort;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;
import com.igormaznitsa.jbbp.model.JBBPFieldUByte;
import com.igormaznitsa.jbbp.model.JBBPFieldUShort;

/**
 * This class converts binary data to JSON format.
 *
 * Parser rules follows Java Binary Block Parser syntax.
 *
 * <p>
 *
 * See details from <a href=
 * "https://github.com/raydac/java-binary-block-parser">https://github.com/raydac/java-binary-block-parser</a>
 *
 * <p>
 * Usage example:
 *
 * <pre>
 * {@code
 * JsonObject json = new Bin2Json("byte a; byte b; ubyte c;").convert("03FAFF");
 * json.toString() = {"a":3,"b":-6,"c":255}
 * </pre>
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public class Bin2Json {

    private final Logger logger = LoggerFactory.getLogger(Bin2Json.class);

    private JBBPParser parser;

    /**
     *
     * @param parserRule Binary data parser rule
     * @throws ConversionException
     */
    public Bin2Json(String parserRule) throws ConversionException {
        try {
            parser = JBBPParser.prepare(parserRule);
        } catch (JBBPException e) {
            throw new ConversionException(String.format("Illegal parser rule, reason: %s", e.getMessage(), e));
        }
    }

    /**
     * Convert {@link String} in hexadecimal string format to JSON object.
     *
     * @param hexString Data in hexadecimal string format. Example data: 03FAFF
     * @return Gson {@link JsonObject}
     * @throws ConversionException
     */
    public JsonObject convert(String hexString) throws ConversionException {
        try {
            return convert(HexUtils.hexToBytes(hexString));
        } catch (IllegalArgumentException e) {
            throw new ConversionException(String.format("Illegal hexstring , reason: %s", e.getMessage(), e));
        }
    }

    /**
     * Convert byte array to JSON object.
     *
     * @param data Data in byte array format.
     * @return Gson {@link JsonObject}
     * @throws ConversionException
     */
    public JsonObject convert(byte[] data) throws ConversionException {
        try {
            return convert(parser.parse(data));
        } catch (IOException e) {
            throw new ConversionException(String.format("Unexpected error, reason: %s", e.getMessage(), e));
        } catch (JBBPException e) {
            throw new ConversionException(String.format("Unexpected error, reason: %s", e.getMessage(), e));
        }
    }

    /**
     * Convert data from {@link InputStream} to JSON object.
     *
     * @param inputStream
     * @return Gson {@link JsonObject}
     * @throws ConversionException
     */
    public JsonObject convert(InputStream inputStream) throws ConversionException {
        try {
            return convert(parser.parse(inputStream));
        } catch (IOException e) {
            throw new ConversionException(String.format("Unexpected error, reason: %s", e.getMessage(), e));
        } catch (JBBPException e) {
            throw new ConversionException(String.format("Unexpected error, reason: %s", e.getMessage(), e));
        }
    }

    private JsonObject convert(JBBPFieldStruct data) throws ConversionException {
        try {
            LocalDateTime start = LocalDateTime.now();
            final JsonObject json = convertToJSon(data);
            if (logger.isTraceEnabled()) {
                Duration duration = Duration.between(start, LocalDateTime.now());
                logger.trace("Conversion time={}, json={}", duration, json.toString());
            }
            return json;
        } catch (JBBPException e) {
            throw new ConversionException(String.format("Unexpected error, reason: %s", e.getMessage(), e));
        }
    }

    private JsonObject convertToJSon(final JBBPAbstractField field) throws ConversionException {
        return convertToJSon(null, field);
    }

    private JsonObject convertToJSon(final JsonObject json, final JBBPAbstractField field) throws ConversionException {
        JsonObject jsn = json == null ? new JsonObject() : json;

        final String fieldName = field.getFieldName() == null ? "nonamed" : field.getFieldName();
        if (field instanceof JBBPAbstractArrayField) {
            final JsonArray jsonArray = new JsonArray();
            if (field instanceof JBBPFieldArrayBit) {
                for (final byte b : ((JBBPFieldArrayBit) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b));
                }
            } else if (field instanceof JBBPFieldArrayBoolean) {
                for (final boolean b : ((JBBPFieldArrayBoolean) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b));
                }
            } else if (field instanceof JBBPFieldArrayByte) {
                for (final byte b : ((JBBPFieldArrayByte) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b));
                }
            } else if (field instanceof JBBPFieldArrayInt) {
                for (final int b : ((JBBPFieldArrayInt) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b));
                }
            } else if (field instanceof JBBPFieldArrayLong) {
                for (final long b : ((JBBPFieldArrayLong) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b));
                }
            } else if (field instanceof JBBPFieldArrayShort) {
                for (final short b : ((JBBPFieldArrayShort) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b));
                }
            } else if (field instanceof JBBPFieldArrayStruct) {
                final JBBPFieldArrayStruct array = (JBBPFieldArrayStruct) field;
                for (int i = 0; i < array.size(); i++) {
                    jsonArray.add(convertToJSon(new JsonObject(), array.getElementAt(i)));
                }
            } else if (field instanceof JBBPFieldArrayUByte) {
                for (final byte b : ((JBBPFieldArrayUByte) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b & 0xFF));
                }
            } else if (field instanceof JBBPFieldArrayUShort) {
                for (final short b : ((JBBPFieldArrayUShort) field).getArray()) {
                    jsonArray.add(new JsonPrimitive(b & 0xFFFF));
                }
            } else {
                throw new ConversionException(String.format("Unexpected field type '%s'", field));
            }
            jsn.add(fieldName, jsonArray);
        } else {
            if (field instanceof JBBPFieldBit) {
                jsn.addProperty(fieldName, ((JBBPFieldBit) field).getAsInt());
            } else if (field instanceof JBBPFieldBoolean) {
                jsn.addProperty(fieldName, ((JBBPFieldBoolean) field).getAsBool());
            } else if (field instanceof JBBPFieldByte) {
                jsn.addProperty(fieldName, ((JBBPFieldByte) field).getAsInt());
            } else if (field instanceof JBBPFieldInt) {
                jsn.addProperty(fieldName, ((JBBPFieldInt) field).getAsInt());
            } else if (field instanceof JBBPFieldLong) {
                jsn.addProperty(fieldName, ((JBBPFieldLong) field).getAsLong());
            } else if (field instanceof JBBPFieldShort) {
                jsn.addProperty(fieldName, ((JBBPFieldShort) field).getAsInt());
            } else if (field instanceof JBBPFieldStruct) {
                final JBBPFieldStruct struct = (JBBPFieldStruct) field;
                final JsonObject obj = new JsonObject();
                for (final JBBPAbstractField f : struct.getArray()) {
                    convertToJSon(obj, f);
                }
                if (json == null) {
                    return obj;
                } else {
                    jsn.add(fieldName, obj);
                }
            } else if (field instanceof JBBPFieldUByte) {
                jsn.addProperty(fieldName, ((JBBPFieldUByte) field).getAsInt());
            } else if (field instanceof JBBPFieldUShort) {
                jsn.addProperty(fieldName, ((JBBPFieldUShort) field).getAsInt());
            } else {
                throw new ConversionException(String.format("Unexpected field '%s'", field));
            }
        }
        return jsn;
    }
}
