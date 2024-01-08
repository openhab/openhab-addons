/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * This interface defines the methods that an implementing class needs to implement to provide the
 * {@link RioHandlerCallback} used by the underlying protocol
 *
 * @author Tim Roberts - Initial contribution
 */
public interface RioCallbackHandler {
    /**
     * Get's the {@link RioHandlerCallback} for the underlying thing
     *
     * @return the {@link RioHandlerCallback} or null if none found
     */
    RioHandlerCallback getRioHandlerCallback();
}
