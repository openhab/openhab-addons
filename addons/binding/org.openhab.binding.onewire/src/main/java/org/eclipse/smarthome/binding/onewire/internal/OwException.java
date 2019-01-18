/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.onewire.internal;

/**
 * The {@link OwException} class defines an exception for handling OneWireExceptions
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwException extends Exception {
    private static final long serialVersionUID = 71120587360960199L;

    public OwException(String message) {
        super(message);
    }
}
