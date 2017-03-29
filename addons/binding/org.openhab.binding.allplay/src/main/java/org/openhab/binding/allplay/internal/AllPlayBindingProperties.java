/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.internal;

import java.util.Dictionary;

/**
 * AllPlay binding properties.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayBindingProperties {

    private final Integer rewindSkipTimeInSec;
    private final Integer fastForwardSkipTimeInSec;
    private final String callbackUrl;

    private static final String REWIND_SKIP_TIME_PROPERTY = "rewindSkipTimeInSec";
    private static final String FAST_FORWARD_SKIP_TIME_PROPERTY = "fastForwardSkipTimeInSec";
    private static final String CALLBACK_URL = "callbackUrl";

    public AllPlayBindingProperties(Dictionary<String, Object> properties) {
        rewindSkipTimeInSec = (Integer) properties.get(REWIND_SKIP_TIME_PROPERTY);
        fastForwardSkipTimeInSec = (Integer) properties.get(FAST_FORWARD_SKIP_TIME_PROPERTY);
        callbackUrl = (String) properties.get(CALLBACK_URL);
    }

    public int getRewindSkipTimeInSec() {
        return rewindSkipTimeInSec;
    }

    public int getFastForwardSkipTimeInSec() {
        return fastForwardSkipTimeInSec;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }
}
