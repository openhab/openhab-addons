/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
