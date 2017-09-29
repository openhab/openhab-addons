/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.internal;

/**
 * Mystrom AuthResponse.
 *
 * @author Stéphane Raemy - Initial contribution
 */
public class AuthResponse {

    public String status;
    public String authToken;
    public String name;
    public String fname;
    public String lname;
    public String email;
    public String accountType;
    public String currency;
    public String onlineShop;
    public String appUrl;

    public AuthResponse() {
    }

}
