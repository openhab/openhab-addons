/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.handlers;

/**
 * The {@link ParadoxPanelConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxPanelConfiguration {

    private int refresh;

    public int getRefresh() {
        return refresh;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }
}
