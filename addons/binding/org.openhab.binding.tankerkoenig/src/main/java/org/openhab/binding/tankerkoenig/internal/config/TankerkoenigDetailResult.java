/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.tankerkoenig.internal.config;

/***
 * The {@link TankerkoenigDetailResult} class is the representing java model for the json result of the tankerkoenig.de
 * details request
 * Actually used for OpeningTimes only.
 *
 * @author Dennis Dollinger/Jürgen Baginski
 *
 */
public class TankerkoenigDetailResult {

    private boolean ok;
    private boolean wholeDay;
    private OpeningTime[] openingtimes;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean iswholeDay() {
        return wholeDay;
    }

    public void setwholeDay(boolean WholeDay) {
        this.wholeDay = WholeDay;
    }

    public OpeningTime[] getOpeningtimes() {
        return openingtimes;
    }

    public void setOpeningtimes(OpeningTime[] openingtimes) {
        this.openingtimes = openingtimes;
    }
}
