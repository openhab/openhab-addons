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
package org.openhab.binding.fsinternetradio.internal.handler;

import org.openhab.binding.fsinternetradio.internal.radio.FrontierSiliconRadio;

/**
 * Utils for the handler.
 *
 * @author Markus Rathgeb - Initial contribution
 */

public class HandlerUtils {

    /**
     * Get the radio of a radio handler.
     *
     * @param handler the handler
     * @return the managed radio object
     */
    public static FrontierSiliconRadio getRadio(final FSInternetRadioHandler handler) {
        return handler.radio;
    }
}
