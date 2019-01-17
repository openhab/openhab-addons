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
import org.openhab.binding.elkm1.internal.elk.ElkMessageFactory;

/**
 * Asks the elk for the zone partitions for all the zones. The partition
 * is which area it is associated with.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ZonePartitionReply extends ElkMessage {
    int[] areas;

    public ZonePartitionReply(String data) {
        super(ElkCommand.ZonePartitionReply);
        areas = new int[ElkMessageFactory.MAX_ZONES];
        byte[] dataBytes = data.getBytes();
        for (int i = 0; i < dataBytes.length && i < ElkMessageFactory.MAX_ZONES; i++) {
            areas[i] = dataBytes[i] - 0x30;
        }
    }

    public int[] getAreas() {
        return areas;
    }

    @Override
    protected String getData() {
        return null;
    }
}
