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
package org.openhab.binding.webthings.internal.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link WebThingsPropertyCommand}
 *
 * @author Sven Schneider - Initial contribution
 */
public class WebThingsPropertyCommand {
    private String id;
    private String messageType;
    private Map<String,Object> data;

    public WebThingsPropertyCommand(String id, String messageType){
        this.id = id;
        this.messageType = messageType;
        this.data = new HashMap<String,Object>();
    }

    public WebThingsPropertyCommand(String messageType){
        this.id = null;
        this.messageType = messageType;
        this.data = new HashMap<String,Object>();
    }
    
    public String getID() { return id; }
    public void setID(String value) { this.id = value; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String value) { this.messageType = value; }
    
    public Map<String,Object> getData() { return data; }
    public void setData(Map<String,Object> value) { this.data = value; }
    public void addData(String key, Object value) { this.data.put(key, value); }
    public void removeData(String key) { if(this.data.containsKey(key)) data.remove(key); }
}
