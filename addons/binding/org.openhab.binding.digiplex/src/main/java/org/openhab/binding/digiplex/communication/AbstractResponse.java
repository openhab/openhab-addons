/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract response
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractResponse implements DigiplexResponse {

    private boolean success;

    public AbstractResponse() {
        this.success = true;
    }

    public AbstractResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
