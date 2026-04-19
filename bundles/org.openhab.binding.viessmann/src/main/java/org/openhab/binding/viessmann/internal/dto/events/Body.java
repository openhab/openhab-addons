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
package org.openhab.binding.viessmann.internal.dto.events;

/**
 * The {@link Body} provides all data of a Body
 *
 * @author Ronny Grun - Initial contribution
 */
public class Body {
    public String errorCode;
    public String deviceId;
    public String modelId;
    public Boolean active;
    public String equipmentType;
    public String errorEventType;
    public String errorDescription;
    public String featureName;
    public String commandName;
    public CommandBody commandBody;
    public Boolean online;
}
