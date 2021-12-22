package org.openhab.binding.mybmw.internal.dto.auth;

import java.util.List;

public class AuthQueryResponse {
    public String clientName;// ": "mybmwapp",
    public String clientSecret;// ": "c0e3393d-70a2-4f6f-9d3c-8530af64d552",
    public String clientId;// ": "31c357a0-7a1d-4590-aa99-33b97244d048",
    public String gcdmBaseUrl;// ": "https://customer.bmwgroup.com",
    public String returnUrl;// ": "com.bmw.connected://oauth",
    public String brand;// ": "bmw",
    public String language;// ": "en",
    public String country;// ": "US",
    public String authorizationEndpoint;// ": "https://customer.bmwgroup.com/oneid/login",
    public String tokenEndpoint;// ": "https://customer.bmwgroup.com/gcdm/oauth/token",
    public List<String> scopes;// ;": [
    // "openid",
    // "profile",
    // "email",
    // "offline_access",
    // "smacc",
    // "vehicle_data",
    // "perseus",
    // "dlm",
    // "svds",
    // "cesim",
    // "vsapi",
    // "remote_services",
    // "fupo",
    // "authenticate_user"
    // ],
    public List<String> promptValues; // ": ["login"]

}
