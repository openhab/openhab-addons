package org.openhab.binding.dbquery.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ActionQueryResult {
    private boolean correct;
    private List<Map<String, Object>> data = Collections.emptyList();

    public ActionQueryResult(boolean correct, @Nullable List<Map<String, Object>> data) {
        this.correct = correct;
        if (data != null)
            this.data = data;
    }

    public boolean isCorrect() {
        return correct;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public @Nullable Object getResultAsScalar() {
        var firstResult = data.get(0);
        return isScalarResult() ? firstResult.get(firstResult.keySet().iterator().next()) : null;
    }

    public boolean isScalarResult() {
        return data.size() == 1 && data.get(0).keySet().size() == 1;
    }
}
