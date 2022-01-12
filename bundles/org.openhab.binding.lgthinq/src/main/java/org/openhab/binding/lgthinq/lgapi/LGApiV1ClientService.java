/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgapi;

import java.util.Map;

import org.openhab.binding.lgthinq.errors.LGApiException;

/**
 * The {@link LGApiV1ClientService}
 *
 * @author Nemer Daud - Initial contribution
 */
public interface LGApiV1ClientService {
    public String startMonitor(String deviceId) throws LGApiException;

    public Map<String, Object> getMonitorData(String workerId) throws LGApiException;
}
