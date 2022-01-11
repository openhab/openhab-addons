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
package org.openhab.binding.evohome.internal;

import java.util.concurrent.TimeoutException;

/**
 * Provides an interface for a delegate that can throw a timeout
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public interface RunnableWithTimeout {

    public abstract void run() throws TimeoutException;
}
