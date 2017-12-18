/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba.model;

/**
 * POJO representation of an operation that the Roomba should perform for the "cmd" command.
 *
 * Example: {"op":"start"}
 *
 * @author Stephen Liang
 *
 */
public class RoombaOperation {
    public String op;

    public RoombaOperation(String op) {
        this.op = op;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }
}
