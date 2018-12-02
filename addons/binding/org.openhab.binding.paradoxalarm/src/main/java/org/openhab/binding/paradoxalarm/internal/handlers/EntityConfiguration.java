/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.handlers;

/**
 * The {@link EntityConfiguration} Common configuration class used by all entities at the moment.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class EntityConfiguration {
    private int id;
    private int refresh;

    public int getId() {
        return id;
    }

    public int getRefresh() {
        return refresh;
    }
}
