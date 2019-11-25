
# Rachio Sprinkler Binding

This binding allows to retrieve status information from Rachio Sprinklers and control some function like run zones, stop watering etc. It uses the Rachio Cloud API, so you need an account and an apikey. You are able to enabled callbacks to receive events like start/stop zones, skip watering etc. However, this requires some network configuration (e.g. port forwarding on the router). The binding could run in polling mode, but then you don't receive those events. 

## Supported Things

The cloud api connector is represented by a Bridge Thing. 
All devices are connected to this thing, all zones to the corresponding device. 

|Thing|Description|
|:---|:---|
|cloud|Each Rachio account is reppresented by a cloud thing. The binding supports multiple accounts at the same time.|
|device|Each sprinkler controller is represented by a device thing, which links to the cloud thing|
|zone|Each zone for each controller creates a zone thing, which links to the device thing (and  directly to the bridge thing)|

## Discovery

You need to register an application and get an API key before the binding could discover your devices. 

- Go to [Rachio Web App](https://rachio.com->login), click on Account Settings in the left navigation. At the bottom you'll find a link "Get API key". 
- Copy the copy and post it to the bridge configuration: apikey=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx.
 
The binding implements monitoring and control functions, but no configuration etc. To change configuration you could use the smartphone App. 

Once the binding is able to connect to the Cloud API it will start the auto-discovery of all controller and zonzes under this account. 
As a result the following things are created

- 1 cloud per account
- 1 device for each controller
- n zones for each zone on any controller

Example: 2 controllers with 8 zones each under the same account creates 19 things (1xbridge, 2xdevice, 16xzone). 

###  Configuration

Option A: Using Paper UI
- Go to Paper UI:Inbox and press the + button.
- Click Add Manually at the end of the list, this will open a list of addable things.
- Select the Rachio Binding
- Select Rachio Cloud Connector things
- Enter at least the api key, other settings are optional
- save

Now the binding is able to connect to the cloud and start discovery devices and zones.

Option B: Using .things file

Create conf/things/rachio.things and fill in the parameters:

```
Bridge rachio:cloud:1 [ apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx", pollingInterval=180, defaultRuntime=120, callbackUrl="https://mydomain.com:443/rachio/webhook", clearAllCallbacks=true  ]
{
}
```

|Parameter|Description|
|:---|:---|
|apikey|This is a token required to access the Rachio Cloud account. See Discovery on information how to get that code.|
|pollingInterval|Specifies the delay between two status polls. Usually something like 10 minutes should be enough to have a regular status update when the interfaces is configured. If you don't want/can use events a smaller delay might be interesting to get quicker responses on running zones etc.|
||Important: Please make sure to use an interval > 90sec. Rachio has a reshhold for the number of API calls per day: 1700. This means if you are accessing the API for more than once in a minute your account gets blocked for the rest of the day.|
|defaultRuntime|You could run zones in 2 different ways:|
||1. Just by pushing the button in your UI. The zone will start watering for &lt;defaultRuntime&gt; seconds.|
||2. Setting the zone's channel runTime to &lt;n&gt; seconds and then starting the zone. This will start the zone for &lt;n&gt; seconds. Usually this variant required a OH rule setting the runTime and then sending a ON to the run channel.|
|callbackUrl|Enable event interface:|
||The Rachio Cloud allows receiving events. For this a callback interface will be provided by the binding (&lt;server&gt;:&lt;port&gt;/rachio/webhook). However, this requires to open a port to the Internet. Please make sure to use https the callback (url starts with "https://") so the transport layer is encrypted. See below how to setup a secure callback.|
||Also notifications have to be enabled if you want to use the event interface. Go to the Rachio Web App-&gt;Accounts Settings-&gt;Notifications|
|clearAllCallbacks|The binding dynamically registers the callback. It also supports multiple applications registered to receive events, e.g. a 2nd OH device with the binding providing the same functionality. If for any reason your device setup changes (e.g. new ip address) you need to clear the registered URL once to avoid the "old URL" still receiving events. This also allows to move for a test setup to the regular setup.|
|ipFilter|Only accept events from the given IP address or subnet list, e.g. '192.168.1.1' or "192.168.1.0/24;192.168.2.0/24"|

The bridge thing doesn't have any channels.

### Device Thing - represents one Rachio Controller

|Channel|Description|
|:--|:--|
|name|Device Name - name of the controller|
|active|ON: Device is active, OFF: Device is deactivated|
|online|ON: Controller is connected to the cloud. OFF: Controller is offline, check internet connection.|
|paused|OFF: Device is in normal run mode; ON: The device is in suspend mode, no schedule is executed|
|stop|ON: Stop watering for all zones (command), OFF: normal operation|
|run|ON: Start warting selected/all zones (defined in runZones)|
|runZones|Zones to run at a time - list, e.g: "1,3" = run zone 1 and 3; "" means: run all zones; Zones will be started by sending ON to the run channel|
|scheduleName|Currently running schedule, if empty no schedule is running|
|devEvent|Receives a JSON-formatted message on each device event from the cloud (requires event callback).|

The are no additional configuration options on the device level.

### Zone Thing - represents one zone of a Controller

| Channel |Description|
|:--|:--|
|number|Zone number as assigned by the controller (zone 1..16)|
|name|Name of the zone as configured in the App.|
|enabled|ON: zone is enabled (ready to run), OFF: zone is disabled.|
|run|ON: The zone starts watering. If runTime is = 0 the defaultRuntime will be used. OFF: Zone stops watering.|
|runTime|Number of seconds to run the zone when run receives ON command|
|runTotal|Total number of seconds the zone was watering (as returned by the cloud service).|
|imageUrl|URL to the zone picture as configured in the App. Rachio supplies default pictures if no image was created. This can be used e.g. in a habPanel to show the zione picture and display the zone name.|
|zoneEvent|Receives a JSON-formatted message on each zone event from the cloud (requires event callback).|


# Full example

## Thing Definition
```
Bridge rachio:cloud:1 @ "Sprinkler" [ apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",  pollingInterval=60, defaultRuntime=120  ]
{
    // Controller
    Thing rachio:device:1:XXXXXXXXXXXX "Rachio-XXXXXX" @ "Sprinkler"
    
    // Zones
    Thing rachio:zone:1:XXXXXXXXXXXX-1 "Rachio zone 1" @ "Sprinkler"
    Thing rachio:zone:1:XXXXXXXXXXXX-2 "Rachio zone 2" @ "Sprinkler"
    Thing rachio:zone:1:XXXXXXXXXXXX-3 "Rachio zone 3" @ "Sprinkler"
    Thing rachio:zone:1:XXXXXXXXXXXX-4 "Rachio zone 4" @ "Sprinkler"
    Thing rachio:zone:1:XXXXXXXXXXXX-5 "Rachio zone 5" @ "Sprinkler"
    Thing rachio:zone:1:XXXXXXXXXXXX-6 "Rachio zone 6" @ "Sprinkler"
    Thing rachio:zone:1:XXXXXXXXXXXX-7 "Rachio zone 7" @ "Sprinkler"
}
```

### Items Definition

```
    // Sprinkler Controller
    String   RachioC04DAC_Name        "Name"                 {channel="rachio:device:1:XXXXXXXXXXXX:name"}
    Switch   RachioC04DAC_Active      "Active"               {channel="rachio:device:1:XXXXXXXXXXXX:active"}
    Switch   RachioC04DAC_Online      "Online"               {channel="rachio:device:1:XXXXXXXXXXXX:online"}
    Switch   RachioC04DAC_Paused      "Paused"               {channel="rachio:device:1:XXXXXXXXXXXX:paused"}
    Switch   RachioC04DAC_Stop        "Stop Watering"        {channel="rachio:device:1:XXXXXXXXXXXX:stop"}
    Switch   RachioC04DAC_Run         "Run Multiple Zones"   {channel="rachio:device:1:XXXXXXXXXXXX:run"}
    String   RachioC04DAC_RunZones    "Run Zone List"        {channel="rachio:device:1:XXXXXXXXXXXX:runZones"}
    Number   RachioC04DAC_RunTime     "Run Time"             {channel="rachio:device:1:XXXXXXXXXXXX:runTime"}
    Number   RachioC04DAC_RainDelay   "Rain Delay"           {channel="rachio:device:1:XXXXXXXXXXXX:rainDelay"}
    String   RachioC04DAC_DevEvent    "Last Device Event"    {channel="rachio:device:1:XXXXXXXXXXXX:devEvent"}
    Number   RachioC04DAC_Latitude    "Latitude"             {channel="rachio:device:1:XXXXXXXXXXXX:latitude"}
    Number   RachioC04DAC_Longitude   "Longitude"            {channel="rachio:device:1:XXXXXXXXXXXX:longitude"}

    // Zone1
    String   RachioZone1_Name        "Zone Name"         {channel="rachio:zone:1:XXXXXXXXXXXX-1:name"}
    Number   RachioZone1_Number      "Zone Number"       {channel="rachio:zone:1:XXXXXXXXXXXX-1:number"}
    Switch   RachioZone1_Enabled     "Zone Enabled"      {channel="rachio:zone:1:XXXXXXXXXXXX-1:enabled"}
    Switch   RachioZone1_Run         "Run Zone"          {channel="rachio:zone:1:XXXXXXXXXXXX-1:run"}
    Number   RachioZone1_RunTime     "Zone Runtime"      {channel="rachio:zone:1:XXXXXXXXXXXX-1:runTime"}
    Number   RachioZone1_RunTotal    "Total Runtime"     {channel="rachio:zone:1:XXXXXXXXXXXX-1:runTotal"}
    String   RachioZone1_ImageUrl    "Zone Image URL"    {channel="rachio:zone:1:XXXXXXXXXXXX-1:imageUrl"}
    String   RachioZone1_ZoneEvent   "Last Zone Event"   {channel="rachio:zone:1:XXXXXXXXXXXX-1:zoneEvent"}

    // Zone2
    String   RachioZone2_Name        "Zone Name"         {channel="rachio:zone:1:XXXXXXXXXXXX-2:name"}
    Number   RachioZone2_Number      "Zone Number"       {channel="rachio:zone:1:XXXXXXXXXXXX-2:number"}
    Switch   RachioZone2_Enabled     "Zone Enabled"      {channel="rachio:zone:1:XXXXXXXXXXXX-2:enabled"}
    Switch   RachioZone2_Run         "Run Zone"          {channel="rachio:zone:1:XXXXXXXXXXXX-2:run"}
    Number   RachioZone2_RunTime     "Zone Runtime"      {channel="rachio:zone:1:XXXXXXXXXXXX-2:runTime"}
    Number   RachioZone2_RunTotal    "Total Runtime"     {channel="rachio:zone:1:XXXXXXXXXXXX-2:runTotal"}
    String   RachioZone2_ImageUrl    "Zone Image URL"    {channel="rachio:zone:1:XXXXXXXXXXXX-2:imageUrl"}
    String   RachioZone2_ZoneEvent   "Last Zone Event"   {channel="rachio:zone:1:XXXXXXXXXXXX-2:zoneEvent"}
```

### Rule Example

```
var Timer rachio_timer5

rule "Start Rachio zone6_2"
    when
        Item ZWaveNode051ZRC90SceneMaster8ButtonRemote_SceneNumber changed
    then
        if (ZWaveNode051ZRC90SceneMaster8ButtonRemote_SceneNumber.state==8.3) {
            if (RachioZone6_Run.state==OFF) {
                // Set zone Quick run time to 1800 sec
                RachioZone6_RunTime.sendCommand(1800)
                RachioZone6_Run.sendCommand(ON)
                ZWaveNode051ZRC90SceneMaster8ButtonRemote_SceneNumber.sendCommand(0.0)
                rachio_timer5 = createTimer(now.plusSeconds(1800)) [|
                RachioZone6_RunTime.sendCommand(120)
                RachioZone6_Run.sendCommand(OFF)
                rachio_timer5 = null
                ]
            }
    else {
                if (RachioZone6_Run.state==ON) {
                    // Change back zone Quick run time to default 120 sec
                    RachioZone6_RunTime.sendCommand(120)
                    RachioZone6_Run.sendCommand(OFF)
                    ZWaveNode051ZRC90SceneMaster8ButtonRemote_SceneNumber.sendCommand(0.0)
                    }
         }  
        }
end
```

### Rule Example

Sample rule to catch zone events (replace rachio_zone_1_xxxxxxxx_1_zoneEvent with the connect item name):

```
var int totalRunTime = 0

/* ------- Alarm handler ----- */
rule "Zone started"
when
    Item rachio_zone_1_xxxxxxxx_1_zoneEvent changed
then
    var jsonString = rachio_zone_1_xxxxxxxx_1_zoneEvent.state.toString
    logDebug("RachioEvent", "Event triggered (JSON='" + jsonString + "')")
    
    var eventTimestamp = transform("JSONPATH","$.timestamp",jsonString);
    var eventType = transform("JSONPATH","$.type",jsonString)
    var eventSubType = transform("JSONPATH","$.subType",jsonString)
    var eventSummary = transform("JSONPATH","$.summary",jsonString)

    if (eventType == "ZONE_STATUS") {
        var zoneName = transform("JSONPATH","$.zoneName",jsonString)
        var zoneNumber = transform("JSONPATH","$.zoneNumber",jsonString)
        var runState = transform("JSONPATH","$.zoneRunState",jsonString)
        var runStart = transform("JSONPATH","$.startTime",jsonString)
        var runEnd = transform("JSONPATH","$.endTime",jsonString)
        var runDuration = transform("JSONPATH","$.duration",jsonString)
        var scheduleType = transform("JSONPATH","$.scheduleType",jsonString)
        logInfo("RachioEvent", eventTimestamp + " " + zoneName + "[" + zoneNumber + "]: " + eventSummary + "(type='" + scheduleType + "', state='" + runState + "')")
        if (eventSubType == "ZONE_COMPLETED") {
            totalRunTime = totalRunTime + Integer::parseInt(runDuration)
            logInfo("RachioZone", "Zone [" + zoneNumber + "]: start: " + runStart + ", end: " + runEnd + ", duration: " + runDuration + " => Total run time = " + totalRunTime)
        }
    } 
    else {
        // generic message
        logInfo("RachioEvent", eventTimestamp + " [" + eventSubType + "]: " + eventSummary)
    }

end
```

# Configuring the Event Callback

The Rachio Cloud API supports an event driven model. The binding registers a so called "web hook" to receive events. In fact this is kind of call callback via http(s). Receiving those notifications requires two things

- the binding listering to a specific URI (in this case /rachio/webhook)
- a port forward on the router to direct inbound requests to the OH device

The router configuration has to be done manually (supporting UPnP-based auto-config is planned, but not yet implemented). In general the following logic applies

- usually openHAB listens on port 8080 for http and 443 for https traffic
- you need to create a forward from a user defined port exposed to the Inernet to the OH ip:8080, e.g. forward external port 50000 tcp to openHAB ip port 8080
  if the router is asking for a port range use the same values external-ip:50000-50000 -&gt; internal_ip:8080-8080
- this results into the callbackUrl https://mydomain.com:50000/rachio/webhook - you need to included this in the thing definition (callbackUrl=xxx), see above

If events are not received (e.g. no events are shown after starting / stopping a zone) the most common reasons is a mis-configuration of the port forwarding. Check openHAB.log, no events are received if you don't see RachioEvent messages. Do the following steps to verify the setup

- make suire to use http instead of https for the moment - https IS NOT supported at the moment
- run a browser and open URL http://127.0.0.1:8080/rachio/webhook - you should get a white screen (no error message) and should the a message in the OH log that the binding is not able to process the request.
- ping your domain and make sure that it returns the external IP of your router
- open URL http://&lt;your domain&gt;:&lt;port&gt;/rachio/webhook - you should see the same page rather than an error

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

The following security pattern is implemented:

- accessing the could API with the reigistered API key
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

- provides general https access to https://&lt;openhab-dnsname&gt;/ using basic auth, but
- https access to https://&lt;openhab-dnsname&gt;/rachio/webhook without user auth. This is important, otherwise the Rachio cloud server couldn't connect to your openhab instance and the Rachio binding.
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
