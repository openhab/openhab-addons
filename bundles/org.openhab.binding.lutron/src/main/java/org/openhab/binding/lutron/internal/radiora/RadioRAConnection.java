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
package org.openhab.binding.lutron.internal.radiora;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface to the RadioRA Classic system
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public interface RadioRAConnection {

    public void open(String portName, int baud) throws RadioRAConnectionException;

    public void disconnect();

    public void write(String command);

    public void setListener(RadioRAFeedbackListener listener);
}
