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
package org.openhab.binding.smhi.internal.model;

import java.util.Date;
import java.util.List;

/**
 * The {@link smhiTimeSeries} is the Java class used to map the JSON response to an SMHI
 * request.
 *
 * @author Michael Parment - Initial contribution
 */

public final class SmhiTimeSeries {
    public Date validTime;

    public List<SmhiParameters> parameters;

    public Date getValidTime() {
        return validTime;
    }

    public List<SmhiParameters> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "ClassPojo [validTime = " + validTime + ", parameters = " + parameters + "]";
    }
}
