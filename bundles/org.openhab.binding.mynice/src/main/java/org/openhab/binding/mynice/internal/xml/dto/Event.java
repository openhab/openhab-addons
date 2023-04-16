/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Event {
    @XStreamAsAttribute
    private String id;
    @XStreamAsAttribute
    private String source;
    @XStreamAsAttribute
    private String target;
    @XStreamAsAttribute
    private String protocolType;
    @XStreamAsAttribute
    private String protocolVersion;
    @XStreamAsAttribute
    public CommandType type;
    @XStreamAlias("Error")
    public Error error;

    @XStreamAlias("Devices")
    private List<Device> devices;

    public @NonNull List<Device> getDevices() {
        List<Device> localDevices = devices;
        return localDevices == null ? List.of() : localDevices;
    }
}
