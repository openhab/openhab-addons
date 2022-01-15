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
package org.openhab.binding.warmup.internal.model.query;

/**
 * @author James Melville - Initial contribution
 */
public class QueryResponseDTO {

    private QueryDataDTO data;
    private String status;

    public QueryDataDTO getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }
}
