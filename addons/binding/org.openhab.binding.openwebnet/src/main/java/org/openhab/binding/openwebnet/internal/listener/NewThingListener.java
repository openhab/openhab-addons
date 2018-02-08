/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.listener;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public interface NewThingListener extends EventListener {

    /**
     * Indicates a new Thing as been found
     *
     * @param where : identifier of thing
     * @param index : position in list of thing managed by bridge
     * @param type : type of the thing
     */
    void onThingFound(int where, int index, int type);

}
