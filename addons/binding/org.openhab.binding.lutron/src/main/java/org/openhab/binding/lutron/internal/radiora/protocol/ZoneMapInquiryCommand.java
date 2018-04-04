/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.Collections;
import java.util.List;

/**
 * Requests an updated ZoneMap.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class ZoneMapInquiryCommand extends RadioRACommand {

    @Override
    public String getCommand() {
        return "ZMPI";
    }

    @Override
    public List<String> getArgs() {
        return Collections.emptyList();
    }

}
