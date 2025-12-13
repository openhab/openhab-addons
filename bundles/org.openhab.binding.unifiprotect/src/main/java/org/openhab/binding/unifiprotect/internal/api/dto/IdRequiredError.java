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
package org.openhab.binding.unifiprotect.internal.api.dto;

import java.util.Map;

/**
 * Specific bad request error when an id path param is missing.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class IdRequiredError {
    public String error;
    public String name;
    public Map<String, Object> cause;
}
