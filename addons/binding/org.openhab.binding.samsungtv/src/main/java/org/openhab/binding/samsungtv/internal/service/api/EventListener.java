/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.service.api;

import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

/**
 * Interface for receiving data from Samsung TV services.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface EventListener {
    /**
     * Invoked when value is received from the TV.
     * 
     * @param variable Name of the variable.
     * @param value Value of the variable value.
     */
    void valueReceived(String variable, State value);

    /**
     * Report an error to this event listener
     *
     * @param statusDetail hint about the actual underlying problem
     * @param message of the error
     * @param e exception that might have occurred
     */
    void reportError(ThingStatusDetail statusDetail, String message, Throwable e);
}
