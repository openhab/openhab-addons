/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal.model;

import org.openhab.binding.foxtrot.internal.ProcessCallback;

/**
 * FoxtrotVariable.
 *
 * @author Radovan Sninsky
 * @since 2018-02-12 23:09
 */
public class Variable {

    private String name;
    private ProcessCallback callback;

    public Variable(String name, ProcessCallback callback) {
        this.name = name;
        this.callback = callback;
    }

    public String getName() {
        return name;
    }

    public ProcessCallback getCallback() {
        return callback;
    }
}
