package org.openhab.binding.habpanelfilter.internal.sse;

import org.osgi.dto.DTO;

public class EventDTO extends DTO {
    public String topic;
    public String payload;
    public String type;

    public EventDTO() {
    }
}
