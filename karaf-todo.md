# distribution

# TODO

## high

## normal

* Test on windows machine (e.g. bin/setenv.bin for http(s) port)
* The bundle org.openhab.core.init contains the logging stuff only. This one is
  not needed for Karaf anymore. Should we remove the whole bundle or leave it
  empty.
* Use at least as much Karaf features / bundles as possible.
  The bundles in the feature openhab2-runtime-split-tp should be replaced with
  dependencies to other Karaf features (e.g. http for jetty).
* The temporary Maven Repo in my (maggu2810) Github account MUST be replaced.

## low

# For Karaf bumps

* If a Karaf version includes
  https://issues.apache.org/jira/browse/KARAF-3982
  we can remove custom system.properties and use "put" to add the properties.
