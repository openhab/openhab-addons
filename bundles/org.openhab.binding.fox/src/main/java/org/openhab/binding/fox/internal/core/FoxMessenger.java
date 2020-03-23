/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoxMessenger} describes communication interface with Fox system.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public interface FoxMessenger {

    public void open() throws FoxException;

    public void write(String text) throws FoxException;

    public String read() throws FoxException;

    public void ping() throws FoxException;

    public void test() throws FoxException;

    public void close() throws FoxException;
}
