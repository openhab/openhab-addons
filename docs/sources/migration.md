# Migration from openHAB 1 to openHAB 2

Note: This page is work in progress and serves as a place to collect whatever you feel is important to mention when migrating your existing setup to openHAB 2.

## Rules

In order to continue using rules from openHAB 1, a few minor changes might be necessary:

1. Import statements at the top are not required anymore for any org.openhab package, so these can be removed
1. The state "Uninitialized" has been renamed to "NULL" and thus needs to be replaced in the rules
