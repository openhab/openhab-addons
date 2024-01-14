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
package org.openhab.binding.homematic.internal.communicator.message;

import java.util.Map;

/**
 * Helper class with common RPC funtions.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class RpcUtils {

    /**
     * Dumps decoded RPC data.
     */
    public static String dumpRpcMessage(String methodName, Object[] responseData) {
        StringBuilder sb = new StringBuilder();
        if (methodName != null) {
            sb.append(methodName);
            sb.append("()\n");
        }
        dumpCollection(responseData, sb, 0);
        return sb.toString();
    }

    private static void dumpCollection(Object[] c, StringBuilder sb, int indent) {
        if (indent > 0) {
            for (int in = 0; in < indent - 1; in++) {
                sb.append('\t');
            }
            sb.append("[\n");
        }
        for (Object o : c) {
            if (o instanceof Map map) {
                dumpMap(map, sb, indent + 1);
            } else if (o instanceof Object[] objects) {
                dumpCollection(objects, sb, indent + 1);
            } else {
                for (int in = 0; in < indent; in++) {
                    sb.append('\t');
                }
                sb.append(o);
                sb.append('\n');
            }
        }
        if (indent > 0) {
            for (int in = 0; in < indent - 1; in++) {
                sb.append('\t');
            }
            sb.append("]\n");
        }
    }

    private static void dumpMap(Map<?, ?> c, StringBuilder sb, int indent) {
        if (indent > 0) {
            for (int in = 0; in < indent - 1; in++) {
                sb.append('\t');
            }
            sb.append("{\n");
        }
        for (Map.Entry<?, ?> me : c.entrySet()) {
            Object o = me.getValue();
            for (int in = 0; in < indent; in++) {
                sb.append('\t');
            }
            sb.append(me.getKey());
            sb.append('=');
            if (o instanceof Map<?, ?> map) {
                sb.append("\n");
                dumpMap(map, sb, indent + 1);
            } else if (o instanceof Object[] objects) {
                sb.append("\n");
                dumpCollection(objects, sb, indent + 1);
            } else {
                sb.append(o);
                sb.append('\n');
            }
        }
        if (indent > 0) {
            for (int in = 0; in < indent - 1; in++) {
                sb.append('\t');
            }
            sb.append("}\n");
        }
    }
}
