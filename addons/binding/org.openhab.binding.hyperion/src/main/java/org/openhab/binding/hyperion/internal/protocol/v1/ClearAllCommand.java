/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.v1;

import org.openhab.binding.hyperion.internal.protocol.HyperionCommand;

/**
 * The {@link ClearAllCommand} is a POJO for sending a Clear All command
 * to the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ClearAllCommand extends HyperionCommand {

    private static final String NAME = "clearall";

    public ClearAllCommand() {
        super(NAME);
    }

}
