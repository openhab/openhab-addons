/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Marco Meyer - Initial contribution
 */
public class ModelParser {
    
    
    private final Gson gson;

    public ModelParser() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MowerStatus.class, new MowerStatusDeserializer());
        gsonBuilder.registerTypeAdapter(MowerMode.class, new MowerModeDeserializer());
        gsonBuilder.registerTypeAdapter(Timer.TimerMode.class, new TimerModeDeserializer());
        this.gson = gsonBuilder.create();         
    }

    public <T>T parse(String jsonString,Class<T> type){
        return gson.fromJson(jsonString, type);
    }
    
    public String exceptionToJSON(Exception e, int errorCode){
        RobonectAnswer answer = new RobonectAnswer().withSuccessful(false).withErrorCode(new Integer(errorCode)).withErrorMessage(e.getMessage());
        return gson.toJson(answer);
    }
}
