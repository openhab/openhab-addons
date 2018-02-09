/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.rego6xx;

/**
 * The {@link ResponseParser} is responsible for parsing arbitrary data coming from a rego 6xx unit.
 *
 * @author Boris Krivonog - Initial contribution
 */
public interface ResponseParser<T> {
    public int responseLength();

    public T parse(byte[] buffer) throws Rego6xxProtocolException;
}
