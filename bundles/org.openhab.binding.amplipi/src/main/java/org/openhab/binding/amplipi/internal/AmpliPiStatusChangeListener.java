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
package org.openhab.binding.amplipi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amplipi.internal.model.Status;

/**
 * This interface is implemented by classes that want to register for any updates from the AmpliPi system.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public interface AmpliPiStatusChangeListener {

    /**
     * This method is called whenever a status update occurs.
     *
     * @param status The current status of the AmpliPi
     */
    void receive(Status status);
}
