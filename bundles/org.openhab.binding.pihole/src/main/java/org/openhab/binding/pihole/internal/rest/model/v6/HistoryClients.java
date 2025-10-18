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
package org.openhab.binding.pihole.internal.rest.model.v6;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public record HistoryClients(Map<String, Client> clients, List<History> history, double took) {
    public record Client(String name, int total) {

    }

    public record History(double timestamp, Map<String, Integer> data) {

    }
}
