/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public class OpenWebNetChannel {

    private final Integer id;
    private final OpenWebNetChannelType type;

    OpenWebNetChannel(int id, int type) {
        this.id = new Integer(id);
        this.type = OpenWebNetChannelType.getType(type);
    }

    public Integer getId() {
        return this.id;
    }

    public OpenWebNetChannelType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "id = " + id.toString() + ", type = " + this.type.toString();
    }

}
