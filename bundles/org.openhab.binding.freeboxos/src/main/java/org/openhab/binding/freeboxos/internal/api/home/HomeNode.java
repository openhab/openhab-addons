package org.openhab.binding.freeboxos.internal.api.home;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

@NonNullByDefault
public class HomeNode {
    public class HomeNodesResponse extends Response<List<HomeNode>> {
    }

    private int id;

    private @Nullable String label;

    private @Nullable String name;

    private @Nullable String category;

    private @Nullable String status;

    private @Nullable List<HomeNodeEndpoint> showEndpoints;

    public int getId() {
        return id;
    }

    public @Nullable String getLabel() {
        return label;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getCategory() {
        return category;
    }

    public @Nullable String getStatus() {
        return status;
    }

    public @Nullable List<HomeNodeEndpoint> getShowEndpoints() {
        return showEndpoints;
    }

}
