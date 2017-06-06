package org.openhab.binding.nest.config;

public class NestBridgeConfiguration {
    /** Client id from the nest product page. */
    public String clientId;
    /** Client secret from the nest product page. */
    public String clientSecret;
    /** Client secret from the auth page. */
    public String pincode;
    /** The access token to use once retrieved from nest. */
    public String accessToken;
    /** How often to refresh data from nest. */
    public int refreshInterval;
}
