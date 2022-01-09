package org.openhab.binding.lgthinq.handler;

import java.util.Map;

public class Gateway {
    private String empBaseUri;
    private String loginBaseUri;
    private String apiRootV1;
    private String apiRootV2;
    private String authBase;
    private String language;
    private String country;
    private String username;
    private String password;

    public Gateway(Map<String,String> params, String language, String country) {
        this.apiRootV2 = params.get("thinq2Uri");
        this.apiRootV1 = params.get("thinq1Uri");
        this.loginBaseUri = params.get("empSpxUri");
        this.authBase =  params.get("empUri");
        this.empBaseUri = params.get("empTermsUri");
        this.language = language;
        this.country = country;
    }

    public String getEmpBaseUri() {
        return empBaseUri;
    }

    public String getApiRootV2() {
        return apiRootV2;
    }

    public String getAuthBase() {
        return authBase;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getLoginBaseUri() {
        return loginBaseUri;
    }

    public String getApiRootV1() {
        return apiRootV1;
    }
}
