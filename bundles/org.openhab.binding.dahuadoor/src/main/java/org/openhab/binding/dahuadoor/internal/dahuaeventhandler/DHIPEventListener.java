package org.openhab.binding.dahuadoor.internal.dahuaeventhandler;

import com.google.gson.JsonObject;

public interface DHIPEventListener {

    public void EventHandler(JsonObject data);

}
