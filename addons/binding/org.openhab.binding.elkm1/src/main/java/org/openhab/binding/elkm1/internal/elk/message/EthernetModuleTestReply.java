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
 * Acknowledge the ethernet response to us.
 *
 * @author David Bennett - Initial Contribution
 */
public class EthernetModuleTestReply extends ElkMessage {
    public EthernetModuleTestReply() {
        super(ElkCommand.EthernetModuleTestAcknowledge);
    }

    @Override
    protected String getData() {
        return "";
    }
}
