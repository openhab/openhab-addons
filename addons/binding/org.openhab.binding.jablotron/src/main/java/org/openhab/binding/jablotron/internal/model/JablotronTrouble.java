/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model;

import com.google.gson.annotations.SerializedName;
import org.openhab.binding.jablotron.internal.model.oasis.OasisLastEntryCID;

/**
 * The {@link OasisLastEntryCID} class defines the OASIS last trouble
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronTrouble {
    private String zekdy;
    private String cas;
    private String message;
    private String name;

    public String getZekdy() {
        return zekdy;
    }

    public String getCas() {
        return cas;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }
}
