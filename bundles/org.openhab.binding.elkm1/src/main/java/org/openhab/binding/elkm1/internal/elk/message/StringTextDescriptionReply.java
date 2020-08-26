/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkTypeToRequest;

/**
 * The response to a request to get the elk message data.
 *
 * @author David Bennett - Initial Contribution
 */
public class StringTextDescriptionReply extends ElkMessage {
    private ElkTypeToRequest typeResponse;
    private int thingNum;
    private String text;

    public StringTextDescriptionReply(String input) {
        super(ElkCommand.StringTextDescriptionReply);
        typeResponse = ElkTypeToRequest.values()[Integer.valueOf(input.substring(0, 2), 16)];
        thingNum = Integer.valueOf(input.substring(2, 5));
        text = input.substring(5).trim();
    }

    public ElkTypeToRequest getTypeResponse() {
        return typeResponse;
    }

    public int getThingNum() {
        return thingNum;
    }

    public String getText() {
        return text;
    }

    @Override
    protected String getData() {
        return null;
    }

}
