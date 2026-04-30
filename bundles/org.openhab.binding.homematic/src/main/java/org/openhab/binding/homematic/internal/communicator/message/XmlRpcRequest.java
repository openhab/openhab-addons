/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.StringUtils;

/**
 * A XML-RPC request for sending data to the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class XmlRpcRequest implements RpcRequest<String> {

    public enum TYPE {
        REQUEST,
        RESPONSE
    }

    private @Nullable String methodName;
    private List<Object> parms;
    private TYPE type;
    public static final SimpleDateFormat XML_RPC_DATEFORMAT = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");

    public XmlRpcRequest(@Nullable String methodName) {
        this(methodName, TYPE.REQUEST);
    }

    public XmlRpcRequest(@Nullable String methodName, TYPE type) {
        this.methodName = methodName;
        this.type = type;
        parms = new ArrayList<>();
    }

    @Override
    public void addArg(Object parameter) {
        parms.add(parameter);
    }

    @Override
    public String createMessage() {
        return toString();
    }

    @Override
    public @Nullable String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml");
        attr(sb, "version", "1.0");
        attr(sb, "encoding", "ISO-8859-1");
        sb.append("?>\n");

        if (type == TYPE.REQUEST) {
            sb.append("<methodCall>");
            tag(sb, "methodName", methodName);
        } else {
            sb.append("<methodResponse>");
        }

        sb.append("\n");
        sb.append("<params>");
        for (Object parameter : parms) {
            sb.append("<param><value>");
            generateValue(sb, parameter);
            sb.append("</value></param>");
        }
        sb.append("</params>");

        if (type == TYPE.REQUEST) {
            sb.append("</methodCall>");
        } else {
            sb.append("</methodResponse>");
        }
        return sb.toString();
    }

    /**
     * Generates a XML attribute.
     */
    private void attr(StringBuilder sb, String name, String value) {
        sb.append(" ").append(name).append("=\"").append(value).append("\"");
    }

    /**
     * Generates a XML tag.
     */
    private void tag(StringBuilder sb, String name, @Nullable String value) {
        sb.append("<").append(name).append(">").append(value).append("</").append(name).append(">");
    }

    /**
     * Generates a value tag based on the type of the value.
     */
    private void generateValue(StringBuilder sb, @Nullable Object value) {
        if (value == null) {
            tag(sb, "string", "void");
        } else {
            Class<?> clazz = value.getClass();
            if (clazz == String.class || clazz == Character.class) {
                sb.append(StringUtils.escapeXml(value.toString()));
            } else if (clazz == Long.class || clazz == Integer.class || clazz == Short.class || clazz == Byte.class) {
                tag(sb, "int", value.toString());
            } else if (clazz == Double.class) {
                tag(sb, "double", String.valueOf(((Double) value).doubleValue()));
            } else if (clazz == Float.class) {
                BigDecimal bd = new BigDecimal((Float) value);
                generateValue(sb, bd.setScale(6, RoundingMode.HALF_DOWN).doubleValue());
            } else if (clazz == BigDecimal.class) {
                generateValue(sb, ((BigDecimal) value).setScale(6, RoundingMode.HALF_DOWN).doubleValue());
            } else if (clazz == Boolean.class) {
                tag(sb, "boolean", ((Boolean) value).booleanValue() ? "1" : "0");
            } else if (clazz == Date.class) {
                synchronized (XML_RPC_DATEFORMAT) {
                    tag(sb, "dateTime.iso8601", XML_RPC_DATEFORMAT.format(((Date) value)));
                }
            } else if (value instanceof Calendar calendar) {
                generateValue(sb, calendar.getTime());
            } else if (value instanceof byte[] bytes) {
                tag(sb, "base64", Base64.getEncoder().encodeToString(bytes));
            } else if (clazz.isArray() || value instanceof List) {
                sb.append("<array><data>");

                Object[] array = null;
                if (value instanceof List list) {
                    array = list.toArray();
                } else {
                    array = (Object[]) value;
                }
                for (Object arrayObject : array) {
                    sb.append("<value>");
                    generateValue(sb, arrayObject);
                    sb.append("</value>");
                }

                sb.append("</data></array>");
            } else if (value instanceof Map) {
                sb.append("<struct>");

                for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    sb.append("<member>");
                    sb.append("<name>").append(entry.getKey()).append("</name>");
                    sb.append("<value>");
                    generateValue(sb, entry.getValue());
                    sb.append("</value>");
                    sb.append("</member>");
                }

                sb.append("</struct>");
            } else {
                throw new IllegalArgumentException("Unsupported XML-RPC Type: " + value.getClass());
            }
        }
    }
}
