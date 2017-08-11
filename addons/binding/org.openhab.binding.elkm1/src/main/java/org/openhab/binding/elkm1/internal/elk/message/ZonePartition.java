/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * Asks the elk for the zone partititions for all the zones. The partition
 * is which area it is associated with.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ZonePartition extends ElkMessage {
    public ZonePartition() {
        super(ElkCommand.ZonePartition);
    }

    @Override
    protected String getData() {
        return "";
    }

}
