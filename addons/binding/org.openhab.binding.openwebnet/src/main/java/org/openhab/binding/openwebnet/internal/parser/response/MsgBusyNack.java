/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.parser.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.listener.ResponseListener;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public final class MsgBusyNack extends Response {

    @Override
    public boolean check(String message) {
        return "*#*6##".equals(message);
    }

    @Override
    public void process(String message, ResponseListener e) {
        e.onBusyNack();
    }
}
