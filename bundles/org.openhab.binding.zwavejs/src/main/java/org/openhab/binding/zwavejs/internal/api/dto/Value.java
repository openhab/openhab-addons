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
package org.openhab.binding.zwavejs.internal.api.dto;

/**
 * @author Leo Siepel - Initial contribution
 */
public class Value {
    public int endpoint;
    public int commandClass;
    public String commandClassName;
    public Object property;
    public String propertyName;
    public int ccVersion;
    public Metadata metadata;
    public Object value;
    public Object propertyKey;
    public String propertyKeyName;
}
