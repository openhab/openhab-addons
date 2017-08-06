/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.config;

/**
 * The {@link JablotronConfig} class defines the bridge configuration
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronConfig {
    private String login;
    private String password;
    private String thingUid;
    private int refresh;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getThingUid() {
        return thingUid;
    }

    public void setThingUid(String thingUid) {
        this.thingUid = thingUid;
    }

    public int getRefresh() {
        return refresh;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }
}
