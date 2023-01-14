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
package org.openhab.binding.homematic.internal.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Result object from a TclRega Script call.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@XStreamAlias("result")
public class HmResult {

    private boolean valid;

    public HmResult() {
    }

    public HmResult(boolean valid) {
        this.valid = valid;
    }

    /**
     * Returns true if the result is valid.
     */
    public boolean isValid() {
        return valid;
    }
}
