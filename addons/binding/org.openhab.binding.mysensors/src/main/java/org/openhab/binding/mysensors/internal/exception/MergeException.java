/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mysensors.internal.exception;

/**
 * Exception is thrown if an error while trying to merge (two nodes or two childs) occures.
 * 
 * @author Andrea Cioni
 * @author Tim Oberföll
 */
public class MergeException extends RuntimeException {
    private static final long serialVersionUID = 6237378516242187660L;

    public MergeException(String message) {
        super(message);
    }
}
