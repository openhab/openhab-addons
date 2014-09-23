
# Eclipse Jetty Configuration


## Running openHAB behind an NGinX reverse proxy

Running openHAB behind a reverse proxy offers you the possibility to access your openHAB runtime via port 80 (http)/ 443 (https). As these port numbers are below 1024 it's not possible for "normal users" to run servers on them. NGinX is a lightweight HTTP server. Of course you can use Apache HTTP server or any other HTTP server which supports reverse proxying as well.

### Running openHAB from a subdomain (like http://openhab.mydomain.tld)

Using this configuration you can proxy connections to http://openhab.domain.tld to your openHAB runtime. You just have to replace openhab.domain.tld with your host name. This host name has to be equal to the one you open in your web browser.

The configuration assumes that you run the reverse proxy on the same machine as your openHAB runtime. If this doesn't fit for you, you just have to replace proxy_pass http://localhost:8080/ by your openHAB runtime hostname (http://youropenhabhostname:8080/).

	server {
		listen 80;
		server_name openhab.domain.tld;
	
		location / {
			proxy_pass                            http://localhost:8080/;
			proxy_set_header Host                 $host;
			proxy_set_header X-Real-IP            $remote_addr;
			proxy_set_header X-Forwarded-For      $proxy_add_x_forwarded_for;
			proxy_set_header X-Forwarded-Scheme   $scheme;		
		}
	}

### Running openHAB from a subdomain with SSL (like https://openhab.mydomain.tld)

Using this configuration you can proxy connections to https://openhab.domain.tld to your openHAB runtime. You just have to replace openhab.domain.tld with your host name. This host name has to be equal to the one you open in your web browser.

Of course you have to reference the SSL certificate (ssl\_certificate) and the private key (ssl\_certificate\_key) that should be used to establish and encrpyt the connection.

The configuration assumes that you run the reverse proxy on the same machine as your openHAB runtime. If this doesn't fit for you, you just have to replace proxy_pass http://localhost:8080/ by your openHAB runtime hostname (http://youropenhabhostname:8080/).

Maybe you do not trust the local network. In this case it's possible to pass the request to openHAB's SSL port using proxy\_pass https://youropenhabhostname:8443/ instead of proxy\_pass http://youropenhabhostname:8080/.

	server {
		listen 443;
		server_name openhab.domain.tld;
		
		ssl on;
		ssl_certificate /etc/nginx/ssl/server.crt;
		ssl_certificate_key /etc/nginx/ssl/server.key;
	
		location / {
			proxy_pass                            http://localhost:8080/;
			proxy_set_header Host                 $host;
			proxy_set_header X-Real-IP            $remote_addr;
			proxy_set_header X-Forwarded-For      $proxy_add_x_forwarded_for;
			proxy_set_header X-Forwarded-Scheme   $scheme;		
		}
	}
	
