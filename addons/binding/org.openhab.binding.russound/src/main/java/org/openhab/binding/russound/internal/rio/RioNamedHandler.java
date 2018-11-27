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
