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
package org.openhab.binding.bsblan.internal.api.models;

import org.openhab.binding.bsblan.internal.api.models.BsbLanApiResponse;
import java.util.HashMap;

/**
 * The {@link BsbLanApiParameterQueryResponse} reflects the response received
 * when querying parameters
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiParameterQueryResponse extends BsbLanApiResponse {

    private HashMap<String, BsbLanApiParameter> parameters;

    public HashMap<String, BsbLanApiParameter> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<String, BsbLanApiParameter>();
        }
        return parameters;
    }

    public void setParameters(HashMap<String, BsbLanApiParameter> parameters) {
        this.parameters = parameters;
    }
}
