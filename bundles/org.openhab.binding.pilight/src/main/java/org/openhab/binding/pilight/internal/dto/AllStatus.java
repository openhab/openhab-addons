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
package org.openhab.binding.pilight.internal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * All status messages.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
public class AllStatus {

    private String message;

    private List<Status> values = new ArrayList<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Status> getValues() {
        return values;
    }

    public void setValues(List<Status> values) {
        this.values = values;
    }
}
