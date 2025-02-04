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
package org.openhab.binding.linky.internal.dto;

import java.util.LinkedHashMap;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class ResponseTempo extends LinkedHashMap<String, String> {
    private static final long serialVersionUID = 362498820763181264L;
}
