
# Rachio Sprinkler Binding

_Release 2.5.0pre2_

This binding allows to retrieve status information from Rachio Sprinklers and control some function like run zones, stop watering etc. It uses the Rachio Cloud API, so you need an account and an apikey. You are able to enabled callbacks to receive events like start/stop zones, skip watering etc. However, this requires some network configuration (e.g. port forwarding on the router). The binding could run in polling mode, but then you don't receive those events. 

## Supported Things

|Thing|Description|
|:---|:---|
|cloud|Each Rachio account is reppresented by a cloud thing. The binding supports multiple accounts at the same time.|
|device|Each sprinkler controller is represented by a device thing, which links to the cloud thing|
|zone|Each zone for each controller creates a zone thing, which links to the device thing (and  directly to the bridge thing)|

## Discovery

The device setup is read from the Cloude setup, so it shares the same items as the Smartphone and Web Apps, so there is no special setup required. In fact all Apps (including this binding) control the same device. The binding implements monitoring and control functions, but no configuration etc. To change configuration you could use the smartphone App. 

As a result the following things are created

- 1 cloud per account
- 1 device for each controller
- n zones for each zone on any controller

Example: 2 controllers with 8 zones each under the same account creates 19 things (1xbridge, 2xdevice, 16xzone). 

## Binding Configuration

If the apikey is configured in the rachio.cfg file a bridge thing is created dynamically and device discovery starts. 

```
# Configuration for the Rachio Binding
# apikey=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx
# callbackUrl="http://mydomain.com:myport/rachio/webhook
# clearAllCallbacks=false
# pollingInterval=120
# defaultRuntime=120
```

See configuration of bridge things below for a description of the config parameters.

## Thing Configuration

### cloud thing - represents a Rachio Cloud account

The cloud api is represented by a Bridge Thing. All devices are connected to this thing, all zones to the corresponding device. Rachio requires an api key created by the user to provide access to the cloud API.

conf/things/rachio.things

```
Bridge rachio:cloud:1 [ apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx", pollingInterval=180, defaultRuntime=120, callbackUrl="http://mydomain.com:50043/rachio/webhook", clearAllCallbacks=true  ]
{
}
```

|Parameter|Description|
|:---|:---|
|apikey|This is a token required to access the Rachio Cloud account. Go to [Rachio Web App](https://rachio.com->login), click on Account Settings in the left navigation. At the bottom you'll find a link "Get API key". Copy the copy and post it to the bridge configuration: apikey=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx.|
|pollingInterval|Specifies the delay between two status polls. Usually something like 10 minutes should be enough to have a regular status update when the interfaces is configured. If you don't want/can use events a smaller delay might be interesting to get quicker responses on running zones etc.|
||Important: Please make sure to use an interval > 90sec. Rachio has a reshhold for the number of API calls per day: 1700. This means if you are accessing the API for more than once in a minute your account gets blocked for the rest of the day.|
|defaultRuntime|You could run zones in 2 different ways:|
||1. Just by pushing the button in your UI. The zone will start watering for  <defaultRuntime> seconds.|
||2. Setting the zone's channel runTime to <n> seconds and then starting the zone. This will start the zone for <n> seconds.<br/>Usually this variant required a OH rule setting the runTime and then sending a ON to the run channel.|
|callbackUrl|Enable event interface:|
||The Rachio Cloud allows receiving events. For this a REST interface will be provided by the binding (<server>:<port>:/rachio/webhook). However, this requires to open a port to the Internet. This can be done by a simple port forwarding from an external port (e.g. 50043) to you OH device. Please make sure to us "http://", so the transport layer is encrypted (https IS NOT YET SUPPORTED).|
||Please make sure that notifications are enabled if you want to use the event interface. Go to the Rachio Web App-&gt;Accounts Settings&Guten Tag,;Notifications|
|clearAllCallbacks|The binding dynamically registers the callback. It also supports multiple applications registered to receive events, e.g. a 2nd OH device with the binding providing the same functionality. If for any reason your device setup changes (e.g. new ip address) you need to clear the registered URL once to avoid the "old URL" still receiving events. This also allows to move for a test setup to the regular setup.|

The bridge thing doesn't have any channels.
<hr/>

### device thing - represents a single Rachio controller

The are no additional configuration options on the device level.

| Channel |Description|
|:--|:--|
|number|Zone number as assigned by the controller (zone 1..16)|
|name|Name of the zone as configured in the App.|
|enabled|ON: zone is enabled (ready to run), OFF: zone is disabled.|
|run|If this channel received ON the zone starts watering. If runTime is = 0 the defaultRuntime will be used.|
|runTime|Number of seconds to run the zone when run receives ON command|
|runTotal|Total number of seconds the zone was watering (as returned by the cloud service).|
|imageUrl|URL to the zone picture as configured in the App. Rachio supplies default pictures if no image was created. This can be used e.g. in a habPanel to show the zione picture and display the zone name.|
|event|This channel receives a JSON-formatted message on each event received from the Rachio Cloud.<br/>Format:|

### zone thing - represents one zone of a controller

The are no additional configuration options on the zone level.


## Configuring the event callback

The Rachio Cloud API supports an event driven model. The binding registeres a so called "web hook" to receive events. In fact this is kind of call callback via http. Receiving those notifications requires two things

- the binding listering to a specific URI (in this case /rachio/webhook)
- a port forward on the router to direct inbound requests to the OH device

The router configuration has to be done manually (supporting UPnP-based auto-config is planned, but not yet implemented). In general the following logic applies

- usually openHAB listens on port 8080 for http traffic
- you need to create a forward from a user defined port exposed to the Inernet to the OH ip:8080, e.g. forward external port 50000 tcp to openHAB ip port 8080
  if the router is asking for a port range use the same values external-ip:50000-50000 -&gt; internal_ip:8080-8080
- this results into the callbackUrl https://mydomain.com:50000/rachio/webhook - you need to included this in the thing definition (callbackUrl=xxx), see above

If events are not received (e.g. no events are shown after starting / stopping a zone) the most common reasons is a mis-configuration of the port forwarding. Check openHAB.log, no events are received if you don't see RachioEvent messages. Do the following steps to verify the setup

- make suire to use http instead of https for the moment - https IS NOT supported at the moment
- run a browser and open URL http://127.0.0.1:8080/rachio/webhook - you should get a white screen (no error message) and should the a message in the OH log that the binding is not able to process the request.
- ping your domain and make sure that it returns the external IP of your router
- open URL http://<your domain>:<port>/rachio/webhook - you should see the same page rather than an error

you could verify the proper registration of the callback after the binding is initialized

- get the deviceId for the controller from the Rachio log entries
- open a terminal window and run 

```
curl -X GET -H "Content-Type: application/json" -H "Authorization: Bearer xxxxxxxx-xxxx-xxxx-xxxx-dc8d5c90350d" https://api.rach.io/1/public/notification/yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyy/webhook
```

replace xxxxxxxx-... with the apikey and yyyyyyyy... with the device id found in the OH log, you should see the configured url
 
### Security

- The document https://support.rachio.com/hc/en-us/articles/115010541948-How-secure-is-my-Rachio-controller describes the Rachio Cloud security.
- The cloud API is hosted on AWS - events are coming from different IPs. Fire-walling could be limited to AWS’ IP address ranges. AWS provides the option to retrieve the current list of ip address ranges: https://docs.aws.amazon.com/general/latest/gr/aws-ip-ranges.html. I’ll will add support to the binding. This list could be loaded when the binding initializes, so the application level IP filtering becomes dynamic. Maybe a daily refresh or so is required to prevent necessary OH restarts when amazon adds more address ranges to the list.

For now the following security pattern is implemented:

- validation of the originator’s IP address (based on up-to-date AWS address ranges - the list is auto-loaded by the binding)
- validation of the URI (/rachio/webhook)
- validation of the externalId (generated by the binding)
- validation of the deviceId / zoneId

## Enhanced security for the webhook event interface - https reverse proxy

A reverse proxy setup is an optional, but important enhancement to the OH security when receiving weekhook events from the Rachio cloud. We can just expose the requested port and do a direct port forwarding, but it's way more secure to limit access to your local OH only for webhook calls (/rachio/webhook).

- This setup will allow for encryption of webhook url call by using a url such as https://example.com:50000/rachio/webhook 
- It is important to note that the encryption is between the Rachio servers and the server that will be performing the proxy in your network. The traffic between the https reverse proxy and openhab will not be encrypted..The webhook will use https, but the reverse proxy forwards this to OH using http (you could also use https, but there is no real need).

### General requirments

There are some pre-requisites to enable the setup. Make sure to have this running before setting up the reverse proxy.

- A NAT rule on your firewall allowing requests from the outside of your network hitting port 50000 to be forwarded to your Apache server.
- A registered domain name with a static IP or a setup where your dynamic IP is automatically updated (dyndns).
- A valid secure certificate. You can use certbot to configure my letsencrypt certificat which is free, works amazingly well, and automatically updates when the certificate is about to expire. For details visit https://certbot.eff.org/
- change the callbackUrl in your .things file to https://example.com:50000/rachio/webhook. 
- If you need to change this url change  clearAllCallbacks=yes restart OH, revert the change and restart again. This makes sure that the latest url gets registered to the Rachio cloud, but the old one is cleared.


### NGINX

Refer to https://docs.openhab.org/installation/security.html#nginx-openssl. This will guide you step-by-step how to setup the NGINX server using https.

This is an example, which

- provides general https access to https://<openhab-dnsname>/ using basic auth, but
- https access to https://<openhab-dnsname>/rachio/webhook without user auth. This is important, otherwise the Rachio cloud server couldn't connect to your openhab instance and the Rachio binding.
- the sample uses strong https setup (see https://docs.openhab.org/installation/security.html#nginx-https-security). You need to perform the additional installation steps to use this sample.
- user auth is also NOT requested when accessing openhab from your local network, see "allow    192.168.0.0/16;"
- the section location /rachio/webhook enabled direct http access to the binding url for the weebhook
- the sample assumes that your are running nginx and openhab on the same node using the OH standard http/https port, otherwise change http://localhost:8080/ and https://localhost:8443/rachio/webhook to your setup.
- the sample redirects all http traffic to https. You don't need to open a http port on the firewall.

#### Requirements

Install nginx, make sure Apache is not installed and no other server is listening to port 80/443.

```
sudo apt-get install nginx
```

Make sure the Raspi's hostname is matching the domain name for the ceriticate:

```
hostname
```

### Configuration

sudo nano /etc/nginx/sites-enabled/openhab

```
server {
    listen                          80;
    server_name                     example.com;
    return 301                      https://$server_name$request_uri;
}

server {
    listen                          443 ssl;
    server_name                     example.com;
    ssl_certificate                 /etc/ssl/openhab.crt;
    ssl_certificate_key             /etc/ssl/openhab.key;
    ssl_protocols                   TLSv1 TLSv1.1 TLSv1.2;
    ssl_prefer_server_ciphers       on;
    ssl_dhparam                     /etc/nginx/ssl/dhparam.pem;
    ssl_ciphers                     ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA384:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA256:ECDHE-RSA-AES256-SHA:HIGH:!aNULL:!eNULL:!LOW:!3DES:!MD5:!EXP:!CBC:!EDH:!kEDH:!PSK:!SRP:!kECDH;
    ssl_session_timeout             1d;
    ssl_session_cache               shared:SSL:10m;
    keepalive_timeout               70;

    location / {
        proxy_pass                            http://localhost:8080/;
        proxy_set_header Host                 $http_host;
        proxy_set_header X-Real-IP            $remote_addr;
        proxy_set_header X-Forwarded-For      $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto    $scheme;

        auth_basic                            "Username and Password Required";
        auth_basic_user_file                  /etc/nginx/.htpasswd;

        satisfy  any;
        allow    192.168.0.0/16;
        allow    127.0.0.1;
        deny     all;
    }

    location /rachio/webhook {
        proxy_pass                            http://localhost:8080/rachio/webhook;
        proxy_set_header Host                 $http_host;
        proxy_set_header X-Real-IP            $remote_addr;
        proxy_set_header X-Forwarded-For      $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto    $scheme;
    }

}
```

After your changed the nginx setup make sure to run

```
sudo nginx -t
sudo service nginx restart
systectl status nginx
```

Check the nginx log for errors:

```
cat  /var/log/nginx/error.log  
```


### Apache

#### Requirements

- Must have an Apache 2 webserver to proxy your requests
- Modules that must be installed in Apache 2 are mod_ssl and rewrite and proxy.

#### Configuration

Decide on a port in the range between 1024 and 65535,for this example we will use port 50000 as shown above.

Let's configure apache to listen for this port using the vi editor. By default your webserver will already be listening on port 80. If you have a previous SSL setup it will also be listening on port 443. Notice how the SSL ports must be in the ssl_module and gnutls sections. You will be adding your port 50000 there and append the https to the end.

sudo nano /etc/apache2/ports.conf

```
Listen 80

<IfModule ssl_module>
        Listen 443
        Listen 50000 https
</IfModule>

<IfModule mod_gnutls.c>
        Listen 443
        Listen 50000 https
</IfModule>
```

Save and close the file

Now you must create an apache configuration file. In this example we use "www.example.com.https.conf" as the file name. Note the configuration below will only accept valid url requests that must be in the following format https://example.com:50000/rachio/webhook. If the url does not match, the webserver will redirect to https://www.openhab.org in this case but you can redirect to the url of your choice. If the URL does match, the apache server will proxy the request to the openhab server. In this case OpenHAb is running on 192.168.1.10 and using port 8080. Let's move on to the configuration.

cd /etc/apache2/sites-available
sudo vim www.example.com.https.conf

```
<IfModule mod_ssl.c>

<VirtualHost *:50000>
        ServerName www.example.com

        <Location /rachio/webhook>
                Order Allow,Deny
                Allow from all
        </Location>

        Include /etc/letsencrypt/options-ssl-apache.conf
        SSLCertificateFile /etc/letsencrypt/live/example.com/fullchain.pem
        SSLCertificateKeyFile /etc/letsencrypt/live/example.com/privkey.pem

        ProxyPreserveHost On
        ProxyPass      /rachio/webhook http://192.168.1.10:8080/rachio/webhook
        ProxyPassReverse /rachio/webhook http://192.168.1.10:8080/rachio/webhook

        RewriteEngine on
        RewriteCond %{REQUEST_URI} !^/rachio/webhook
        RewriteRule ^(.*)$ https://www.openhab.org// [L,R=301]

</VirtualHost>

</IfModule>
```

To activate the new configuration, you need to run:

```
systemctl reload apache2
 ```
 
If you see no errors on reload, everything should be working so go ahead and make a request from outside your network to your URL... https://example.com:50000/rachio/webhook You should see a white screen just as you do without the https. 

You can also try a request to https://example.com:50000/anythinghere and it should redirect to the URL of your choice when it doesnt hit the webhook URL. This is just to distract bad guys from trying random urls.
