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
package org.eclipse.smarthome.binding.fsinternetradio.handler;

import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadio;

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
