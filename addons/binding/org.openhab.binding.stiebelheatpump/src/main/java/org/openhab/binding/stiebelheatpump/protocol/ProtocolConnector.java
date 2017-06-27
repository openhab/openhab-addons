/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.stiebelheatpump.protocol;

import org.openhab.binding.stiebelheatpump.internal.StiebelHeatPumpException;

public interface ProtocolConnector {

    public abstract void connect(String s, int i) throws StiebelHeatPumpException;

    public abstract void disconnect();

    public abstract byte get() throws StiebelHeatPumpException;

    public abstract short getShort() throws StiebelHeatPumpException;

    public abstract void get(byte abyte0[]) throws StiebelHeatPumpException;

    public abstract void mark();

    public abstract void reset();

    public abstract void write(byte abyte0[]) throws StiebelHeatPumpException;

    public abstract void write(byte byte0) throws StiebelHeatPumpException;
}
