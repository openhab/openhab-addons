/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * Get the version number from the elk.
 *
 * @author David Bennett - Initial Contribution
 */
public class Version extends ElkMessage {
    public Version() {
        super(ElkCommand.RequestVersionNumber);
    }

    @Override
    protected String getData() {
        return "";
    }
}
