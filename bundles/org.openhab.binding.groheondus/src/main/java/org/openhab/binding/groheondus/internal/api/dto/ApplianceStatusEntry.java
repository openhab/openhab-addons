/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal.api.dto;

/**
 * One entry of the appliance status endpoint, e.g. type {@code battery}.
 *
 * @author Arne Seime - Initial contribution
 */
public class ApplianceStatusEntry {
    public String type;
    public String value;
}
