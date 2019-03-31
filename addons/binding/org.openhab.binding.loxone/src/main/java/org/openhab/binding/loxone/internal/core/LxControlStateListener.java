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
package org.openhab.binding.loxone.internal.core;

/**
 * This is an interface to listen to control's state changes
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
interface LxControlStateListener {
    /**
     * This method will be called by registered listener, when control's state is changed
     *
     * @param state
     *            changed state
     */
    void onStateChange(LxControlState state);
}
