
package org.openhab.binding.wlanthermo.internal.api.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Iot {

    @SerializedName("PMQhost")
    @Expose
    public String pMQhost;
    @SerializedName("PMQport")
    @Expose
    public Integer pMQport;
    @SerializedName("PMQuser")
    @Expose
    public String pMQuser;
    @SerializedName("PMQpass")
    @Expose
    public String pMQpass;
    @SerializedName("PMQqos")
    @Expose
    public Integer pMQqos;
    @SerializedName("PMQon")
    @Expose
    public Boolean pMQon;
    @SerializedName("PMQint")
    @Expose
    public Integer pMQint;
    @SerializedName("CLon")
    @Expose
    public Boolean cLon;
    @SerializedName("CLtoken")
    @Expose
    public String cLtoken;
    @SerializedName("CLint")
    @Expose
    public Integer cLint;
    @SerializedName("CLurl")
    @Expose
    public String cLurl;

}
