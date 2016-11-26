/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal.commands;

import java.math.BigDecimal;
import java.util.List;

/**
 * Command to retrieve current in Ampere.
 *
 * @author Falk Harnisch - Initial Contributionh
 */
public class GetCurrent extends AbstractCMDNowPowerCommand<BigDecimal> {

    @Override
    protected List<String> getPath() {
        List<String> list = super.getPath();
        list.add("Device.System.Power.NowCurrent");
        return list;
    }

}
