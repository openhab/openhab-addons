/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Stop Raise Lower (STOPRL)
 * Stop Raising or Lowering.
 * 
 * @author Jeff Lauterbach
 *
 */
public class StopRaisingOrLoweringCommand extends RadioRACommand {

    @Override
    public String getCommand() {
        return "STOPRL";
    }

    @Override
    public List<String> getArgs() {
        return new ArrayList<>();
    }

}
