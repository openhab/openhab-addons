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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Authentication {
    public enum UserPerm {
        wait,
        user,
        admin
    }

    @XStreamAsAttribute
    public int id;
    @XStreamAsAttribute
    public String pwd;
    @XStreamAsAttribute
    private String username;
    @XStreamAsAttribute
    public UserPerm perm;
    @XStreamAsAttribute
    public boolean notify;
    @XStreamAsAttribute
    public String sc;
}
