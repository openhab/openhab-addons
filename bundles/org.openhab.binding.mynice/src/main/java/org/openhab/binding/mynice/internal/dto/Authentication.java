package org.openhab.binding.mynice.internal.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("Authentication")
public class Authentication {
    @XStreamAsAttribute
    private String id;
    @XStreamAsAttribute
    public String pwd;
    @XStreamAsAttribute
    private String username;

    @XStreamAsAttribute
    public UserPerm perm;
    @XStreamAsAttribute
    public boolean notify;

    @XStreamAsAttribute
    public String sc;

    @Override
    public String toString() {
        return "Authentication [id = " + id + ", pwd = " + pwd + ", username = " + username + "]";
    }
}
