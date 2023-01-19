#!/bin/bash

# rsync -avz --exclude="internal/netty"  org.openhab.binding.dahuacamera root@fishernet:/root/openhab-devel/openhab-addons/bundles
# rsync -avz --exclude="internal/netty"  org.openhab.binding.anpvizcamera root@fishernet:/root/openhab-devel/openhab-addons/bundles
 rsync -avz --exclude="internal/netty"  org.openhab.binding.gpio root@fishernet:/root/openhab-devel/openhab-addons/bundles
