package org.openhab.binding.freeboxos.internal.api.home;

public class HomeNodeEndpoint {

    private int id;

    private String label;

    private String name;

    private String epType;

    private String visibility;

    private String valueType;

    private int refresh;

    private HomeNodeEndpointUi ui;

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getEpType() {
        return epType;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getValueType() {
        return valueType;
    }

    public long getRefresh() {
        return refresh;
    }

    public HomeNodeEndpointUi getUi() {
        return ui;
    }
}
