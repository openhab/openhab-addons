/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

/**
 * The {@link NetworkError} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class NetworkError {
    public String url;
    public int status;
    public String reason;
    public String params;

    @Override
    public String toString() {
        return new StringBuilder(url).append(Constants.HYPHEN).append(status).append(Constants.HYPHEN).append(reason)
                .append(params).toString();
    }

    public String toJson() {
        return Converter.getGson().toJson(this);
    }
}
