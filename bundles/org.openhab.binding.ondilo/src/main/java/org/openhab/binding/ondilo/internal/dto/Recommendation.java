/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ondilo.internal.dto;

/**
 * The {@link Recommendation} DTO for representing Ondilo recommendations.
 *
 * @author MikeTheTux - Initial contribution
 */
public class Recommendation {
    /*
     * "id": 10251,
     * "title": "Clean your filter",
     * "message": "I recommend you to regularly clean your filter in order to evacuate the impurities retained and ensure its efficiency.",
     * "created_at": "2020-03-25T04:09:59+0000",
     * "updated_at": "2020-03-25T04:09:59+0000",
     * "status": "waiting",
     * "deadline": "2020-03-26T00:00:00+0000"
     */
    public int id;
    public String title;
    public String message;
    public String created_at;
    public String updated_at;
    public String status;
    public String deadline;
}
