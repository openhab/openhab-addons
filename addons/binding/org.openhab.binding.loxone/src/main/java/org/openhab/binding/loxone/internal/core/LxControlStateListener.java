/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
