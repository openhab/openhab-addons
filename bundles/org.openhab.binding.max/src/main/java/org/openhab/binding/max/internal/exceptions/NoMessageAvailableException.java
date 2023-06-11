/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.exceptions;

/**
 * Will be thrown when there is an attempt to pull a message from the message processor,
 * but the processor does not yet have a complete message.
 *
 * @author Christian Rockrohr <christian@rockrohr.de> - Initial contribution
 */
public class NoMessageAvailableException extends Exception {

    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final long serialVersionUID = -7663390696233390452L;
}
