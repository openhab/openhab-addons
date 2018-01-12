/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.rego6xx;

/**
 * The {@link Rego6xxProtocolException} is responsible for holding information about an Rego6xx protocol error.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class Rego6xxProtocolException extends Exception {

    private static final long serialVersionUID = 7556083982084149686L;

    public Rego6xxProtocolException(String message) {
        super(message);
    }
}
