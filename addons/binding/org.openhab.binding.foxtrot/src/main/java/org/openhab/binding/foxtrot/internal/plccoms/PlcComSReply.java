/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal.plccoms;

import java.math.BigDecimal;

/**
 * PlcVariableValue.
 *
 * @author Radovan Sninsky
 * @since 2018-03-30 22:56
 */
public class PlcComSReply {

    private String op;
    private String name;
    private String value;

    PlcComSReply(String op, String name, String value) {
        this.op = op;
        this.name = name;
        this.value = value;
    }

    public String getOp() {
        return op;
    }

    public String getName() {
        return name;
    }

    public String getString() {
        return value != null && value.startsWith("\"") && value.endsWith("\"") ?
                value.substring(1, value.length()-1) : value;
    }

    public BigDecimal getNumber() {
        try {
            return value != null ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean getBool() {
        if ("0".equals(value) || "1".equals(value)) {
            return "0".equals(value) ? Boolean.FALSE : Boolean.TRUE;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlcComSReply{");
        sb.append("op='").append(op).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
