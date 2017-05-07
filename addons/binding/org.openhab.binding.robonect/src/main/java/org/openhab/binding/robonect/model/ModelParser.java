package org.openhab.binding.robonect.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModelParser {
    
    
    private Gson gson;

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
