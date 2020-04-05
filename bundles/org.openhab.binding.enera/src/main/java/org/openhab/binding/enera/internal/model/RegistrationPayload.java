package org.openhab.binding.enera.internal.model;

public class RegistrationPayload {
    private String DeviceId;
    private String ClientId;
    private String JWT;

    public RegistrationPayload() {
    }

    public RegistrationPayload(String deviceId, String clientId, String jwt) {
        this.DeviceId = deviceId;
        this.ClientId = clientId;
        this.JWT = jwt;
    }

    /**
     * @return the deviceId
     */
    public String getDeviceId() {
        return DeviceId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return ClientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    /**
     * @return the jWT
     */
    public String getJWT() {
        return JWT;
    }

    /**
     * @param jWT the jWT to set
     */
    public void setJWT(String jWT) {
        JWT = jWT;
    }
}
