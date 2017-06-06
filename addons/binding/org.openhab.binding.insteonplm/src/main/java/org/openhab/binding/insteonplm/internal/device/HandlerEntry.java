package org.openhab.binding.insteonplm.internal.device;

import java.util.Map;

/**
 * Keeps track of the details about the handler.
 * 
 * @author David Bennett - Initial Contribution
 */
public class HandlerEntry {
    private final String handlerName;
    private final Map<String, String> params;

    public HandlerEntry(String name, Map<String, String> params) {
        this.handlerName = name;
        this.params = params;
    }

    /**
     * The name of the handler.
     *
     * @return the name of the handler
     */
    public String getHandlerName() {
        return handlerName;
    }

    /**
     * The parameters associated with the handler.
     *
     * @return the params associated with the handler
     */
    public Map<String, String> getParameters() {
        return params;
    }

}
