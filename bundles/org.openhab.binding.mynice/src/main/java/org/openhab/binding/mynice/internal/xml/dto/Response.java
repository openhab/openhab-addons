/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal.xml.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Response extends Event {
    @XStreamAlias("Authentication")
    public Authentication authentication;
    @XStreamAlias("Interface")
    public Interface intf;
}
