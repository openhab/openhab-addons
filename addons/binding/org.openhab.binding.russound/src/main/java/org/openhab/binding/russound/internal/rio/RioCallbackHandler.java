/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
