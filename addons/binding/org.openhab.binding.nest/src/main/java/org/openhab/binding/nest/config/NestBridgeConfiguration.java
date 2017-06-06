package org.openhab.binding.nest.config;

public class NestBridgeConfiguration {
    /** Client id from the nest product page. */
    public String clientId;
    /** Client secret from the nest product page. */
    public String clientSecret;
    /** Client secret from the auth page. */
    public String pincode;
    /** How often to refresh data from nest. */
    public int refreshInterval;
}
