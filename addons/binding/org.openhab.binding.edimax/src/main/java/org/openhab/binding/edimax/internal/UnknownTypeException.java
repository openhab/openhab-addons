/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal;

/**
 * This Exception will be thrown, if the return type is unknown.
 *
 * @author Falk Harnisch - Initial Contribution
 *
 */
public class UnknownTypeException extends RuntimeException {

    private static final long serialVersionUID = 4826720539738874681L;

    public UnknownTypeException(String message) {
        super(message);
    }
}
