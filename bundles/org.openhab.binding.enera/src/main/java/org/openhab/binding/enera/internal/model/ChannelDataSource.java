package org.openhab.binding.enera.internal.model;

public class ChannelDataSource {
    private String obisKey;
    private AggregationType aggregationType;

    public ChannelDataSource() {
    }

    public ChannelDataSource(String obisKey, AggregationType aggregationType) {
        this.obisKey = obisKey;
        this.aggregationType = aggregationType;
    }

    public String getObisKey() {
        return obisKey;
    }

    public void setObisKey(String value) {
        this.obisKey = value;
    }

    public AggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

}
