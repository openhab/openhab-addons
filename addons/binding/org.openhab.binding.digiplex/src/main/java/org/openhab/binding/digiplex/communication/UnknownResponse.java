/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

/**
 * Unknown message from PRT3
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class UnknownResponse implements DigiplexResponse {

    private String message;

    public UnknownResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleUnknownResponse(this);
    }

}
