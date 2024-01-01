/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.protocol;

/**
 * Performs conversion logic between canonical input names and underlying Yamaha protocol.
 *
 * For example, AVRs when setting input 'AUDIO_X' (or HDMI_X) need the input to be sent in this form.
 * However, what comes back in the status update from the AVR is 'AUDIOX' (and 'HDMIX') respectively.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public interface InputConverter {

    /**
     * Converts the canonical input name to name used by the protocol
     *
     * @param name canonical name
     * @return command name
     */
    String toCommandName(String name);

    /**
     * Converts the state name used by the protocol to canonical input name
     *
     * @param name state name
     * @return canonical name
     */
    String fromStateName(String name);
}
