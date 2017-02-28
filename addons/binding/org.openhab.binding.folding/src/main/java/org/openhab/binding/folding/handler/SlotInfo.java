package org.openhab.binding.folding.handler;

import java.util.Map;

public class SlotInfo {

    // Entity for Json de-serialization

    public String id, status, description, reason;
    public Map<String, String> options;
    boolean idle;

}
