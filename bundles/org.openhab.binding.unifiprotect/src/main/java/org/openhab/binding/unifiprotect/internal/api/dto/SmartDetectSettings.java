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
package org.openhab.binding.unifiprotect.internal.api.dto;

import java.util.List;

/**
 * Smart detection settings for a camera.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SmartDetectSettings {
    public List<ObjectType> objectTypes;
    public List<AudioType> audioTypes;
}
