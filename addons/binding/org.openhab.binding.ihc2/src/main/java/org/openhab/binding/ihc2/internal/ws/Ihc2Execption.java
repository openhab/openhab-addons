/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws;

/**
 * Exception for handling communication errors to controller.
 *
 * @author Pauli Anttila
 * @since 1.5.0
 */
public class Ihc2Execption extends Exception {

    private static final long serialVersionUID = -8048415193494625295L;

    public Ihc2Execption() {
        super();
    }

    public Ihc2Execption(String message) {
        super(message);
    }

    public Ihc2Execption(String message, Throwable cause) {
        super(message, cause);
    }

    public Ihc2Execption(Throwable cause) {
        super(cause);
    }

}
