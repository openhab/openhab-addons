/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.protocol;

/**
 * Base class for Feedback from RadioRA
 * 
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class RadioRAFeedback {

    public String[] parse(String msg, int numParams) {

        String[] params = msg.split(",");
        if (params.length < numParams + 1) {
            throw new IllegalStateException("Invalid message format: " + msg);
        }

        return params;
    }

}
