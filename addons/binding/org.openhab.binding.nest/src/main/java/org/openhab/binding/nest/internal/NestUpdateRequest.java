package org.openhab.binding.nest.internal;

import java.util.Map;

/**
 * Contains the data needed to do an update request back to nest.
 *
 * @author David Bennett
 */
public class NestUpdateRequest {
    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void addValue(String key, Object value) {
        values.put(key, value);
    }

    private String updateUrl;
    private Map<String, Object> values;
}
