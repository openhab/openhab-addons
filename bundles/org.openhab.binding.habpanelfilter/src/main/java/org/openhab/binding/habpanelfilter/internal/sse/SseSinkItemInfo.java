package org.openhab.binding.habpanelfilter.internal.sse;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SseSinkItemInfo {
    private final String connectionId = UUID.randomUUID().toString();
    private final Set<String> trackedItems = new CopyOnWriteArraySet();

    public SseSinkItemInfo() {
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public void updateTrackedItems(Set<String> itemNames) {
        this.trackedItems.clear();
        this.trackedItems.addAll(itemNames);
    }

    public static Predicate<SseSinkItemInfo> hasConnectionId(String connectionId) {
        return (info) -> {
            return info.connectionId.equals(connectionId);
        };
    }

    public static Predicate<SseSinkItemInfo> tracksItem(String itemName) {
        return (info) -> {
            return info.trackedItems.contains(itemName);
        };
    }
}
