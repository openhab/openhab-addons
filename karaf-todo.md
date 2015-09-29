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

* 4.0.2: remove config.properties
  config.properties is currently used to remove the javax.annotation manually.
  This is fixed with 4.0.2.
  ===
  [SOLVED] There is a problem with Jersey and at least the PaperUI.
  https://github.com/hstaudacher/osgi-jax-rs-connector/issues/106
  The config.properties for Karaf 4.0.1 states that the OSGi framework exports
  javax.annotation;version=1.2. This is wrong and needs to be removed.
  It will be fixed in Karaf 4.0.2, we could remove the line or wait for bump.
  https://github.com/apache/karaf/commit/59c11e4deebd6940136149a44f8485a941653e28

* If a Karaf version includes
  https://issues.apache.org/jira/browse/KARAF-3982
  we can remove custom system.properties and use "put" to add the properties.
