package org.openhab.binding.homie.internal;

import java.util.Dictionary;

public class HomieConfiguration {

    private String baseTopic;

    private String brokerUrl;

    public HomieConfiguration(Dictionary<String, Object> properties) {
        brokerUrl = (String) properties.get("mqttbrokerurl");
        baseTopic = (String) properties.get("basetopic");
    }

    public String getBaseTopic() {
        return baseTopic;
    }

    public void setBaseTopic(String baseTopic) {
        this.baseTopic = baseTopic;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

}
