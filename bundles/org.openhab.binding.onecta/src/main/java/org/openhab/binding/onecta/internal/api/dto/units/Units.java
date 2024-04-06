package org.openhab.binding.onecta.internal.api.dto.units;

import java.util.ArrayList;
import java.util.List;

public class Units {
    private List<Unit> units;

    public Units() {
        this.units = new ArrayList<>();
    }

    public List<Unit> getAll() {
        return this.units;
    }

    public Unit get(int index) {
        return this.units.get(index);
    }

    public Unit findById(String key) {
        return units.stream().filter(unit -> key.equals(unit.getId().toString())).findFirst().orElse(null);
    }
}
