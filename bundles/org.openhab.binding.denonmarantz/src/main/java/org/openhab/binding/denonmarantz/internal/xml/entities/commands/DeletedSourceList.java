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
package org.openhab.binding.denonmarantz.internal.xml.entities.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to unmarshall {@code <list>} items of the {@code <functiondelete>} CommandRX.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 */
@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
@NonNullByDefault
public class DeletedSourceList {

    private @Nullable String name;

    private @Nullable String funcName;

    private @Nullable Integer use;

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getFuncName() {
        return funcName;
    }

    public @Nullable Integer getUse() {
        return use;
    }
}
