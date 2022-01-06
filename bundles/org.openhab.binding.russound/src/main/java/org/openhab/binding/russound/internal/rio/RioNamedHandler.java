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
package org.openhab.binding.russound.internal.rio;

/**
 * Interface for any handler that supports an identifier and name
 *
 * @author Tim Roberts - Initial contribution
 */
public interface RioNamedHandler {
    /**
     * Returns the ID of the handler
     *
     * @return the identifier of the handler
     */
    int getId();

    /**
     * Returns the name of the handler
     *
     * @return
     */
    String getName();
}
