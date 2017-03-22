/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.exception;

/**
 * Exception occurs if there is no content to add, for example in a variable.
 *
 * @author Tim Oberföll
 *
 */
public class NoContentException extends Exception {
    private static final long serialVersionUID = -3446354234423332363L;

    public NoContentException(String message) {
        super(message);
    }
}
