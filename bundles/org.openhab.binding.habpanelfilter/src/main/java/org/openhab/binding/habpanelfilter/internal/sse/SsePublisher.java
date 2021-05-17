package org.openhab.binding.habpanelfilter.internal.sse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.Event;

@NonNullByDefault
public interface SsePublisher {
    void broadcast(Event var1);
}
