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

/**
 * A XML-RPC request for sending data to the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class XmlRpcRequest implements RpcRequest<String> {

    public enum TYPE {
        REQUEST,
        RESPONSE
    }

    private String methodName;
    private List<Object> parms;
    private StringBuilder sb;
    private TYPE type;
    public static final SimpleDateFormat XML_RPC_DATEFORMAT = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");

    public XmlRpcRequest(String methodName) {
        this(methodName, TYPE.REQUEST);
    }

    public XmlRpcRequest(String methodName, TYPE type) {
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
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        sb = new StringBuilder();

        sb.append("<?xml");
        attr("version", "1.0");
        attr("encoding", "ISO-8859-1");
        sb.append("?>\n");

        if (type == TYPE.REQUEST) {
            sb.append("<methodCall>");
            tag("methodName", methodName);
        } else {
            sb.append("<methodResponse>");
        }

        sb.append("\n");
        sb.append("<params>");
        for (Object parameter : parms) {
            sb.append("<param><value>");
            generateValue(parameter);
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
    private void attr(String name, String value) {
        sb.append(" ").append(name).append("=\"").append(value).append("\"");
    }

    /**
     * Generates a XML tag.
     */
    private void tag(String name, String value) {
        sb.append("<").append(name).append(">").append(value).append("</").append(name).append(">");
    }

    /**
     * Generates a value tag based on the type of the value.
     */
    private void generateValue(Object value) {
        if (value == null) {
            tag("string", "void");
        } else {
            Class<?> clazz = value.getClass();
            if (clazz == String.class || clazz == Character.class) {
                sb.append(escapeXml(value.toString()));
            } else if (clazz == Long.class || clazz == Integer.class || clazz == Short.class || clazz == Byte.class) {
                tag("int", value.toString());
            } else if (clazz == Double.class) {
                tag("double", String.valueOf(((Double) value).doubleValue()));
            } else if (clazz == Float.class) {
                BigDecimal bd = new BigDecimal((Float) value);
                generateValue(bd.setScale(6, RoundingMode.HALF_DOWN).doubleValue());
            } else if (clazz == BigDecimal.class) {
                generateValue(((BigDecimal) value).setScale(6, RoundingMode.HALF_DOWN).doubleValue());
            } else if (clazz == Boolean.class) {
                tag("boolean", ((Boolean) value).booleanValue() ? "1" : "0");
            } else if (clazz == Date.class) {
                synchronized (XML_RPC_DATEFORMAT) {
                    tag("dateTime.iso8601", XML_RPC_DATEFORMAT.format(((Date) value)));
                }
            } else if (value instanceof Calendar calendar) {
                generateValue(calendar.getTime());
            } else if (value instanceof byte[] bytes) {
                tag("base64", Base64.getEncoder().encodeToString(bytes));
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
                    generateValue(arrayObject);
                    sb.append("</value>");
                }

                sb.append("</data></array>");
            } else if (value instanceof Map) {
                sb.append("<struct>");

                for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    sb.append("<member>");
                    sb.append("<name>").append(entry.getKey()).append("</name>");
                    sb.append("<value>");
                    generateValue(entry.getValue());
                    sb.append("</value>");
                    sb.append("</member>");
                }

                sb.append("</struct>");
            } else {
                throw new RuntimeException("Unsupported XML-RPC Type: " + value.getClass());
            }
        }
    }

    private StringBuilder escapeXml(String inValue) {
        StringBuilder outValue = new StringBuilder(inValue.length());
        for (int i = 0; i < inValue.length(); i++) {
            switch (inValue.charAt(i)) {
                case '<':
                    outValue.append("&lt;");
                    break;
                case '>':
                    outValue.append("&gt;");
                    break;
                case '&':
                    outValue.append("&amp;");
                    break;
                case '\'':
                    outValue.append("&apost;");
                    break;
                case '"':
                    outValue.append("&quot;");
                    break;
                default:
                    outValue.append(inValue.charAt(i));
            }
        }
        return outValue;
    }
}
