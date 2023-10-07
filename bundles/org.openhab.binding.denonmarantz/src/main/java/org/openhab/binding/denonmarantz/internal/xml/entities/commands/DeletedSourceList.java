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
package org.openhab.binding.denonmarantz.internal.xml.entities.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used to unmarshall {@code <list>} items of the {@code <functiondelete>} CommandRX.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 */
@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeletedSourceList {

    private String name;

    private String funcName;

    private Integer use;

    public String getName() {
        return name;
    }

    public String getFuncName() {
        return funcName;
    }

    public Integer getUse() {
        return use;
    }
}
