# Paper UI 

The Paper UI is an HTML5 web application. The Paper UI implements Google's Material Design and is responsive, so that it smoothly renders on different screen sizes. All modern browsers (Safari, Chrome, Firefox) besides the Internet Explorer are supported in their newest version. The Internet Explorer is mainly lacking support for SSE.

Note that the Paper UI currently only supports limited use cases. It is mainly meant for setup and administration purposes, not for operation, for which you should rather refer to the [Basic UI](../basic/readme.html).

Even for setup and administration purposes, there are many features not yet available, which can be done through textual configuration, i.e. complex item definitions with grouping, sitemap definitions, textual rules and scripts, configuration of persistence, etc.
Therefore power users are advised to prefer textual configuration over the pure use of the Paper UI.

## Features

The following features are implemented:

* Inbox & discovery of Things
* Manual setup of Things
* Binding information
* Configuration of Things
* Configuration of services
* Event support for item state updates, Thing status updates and new inbox entries


## FAQ
 
### Why is it named Paper UI?
 
Google's Material Design approach uses so called "cards", which looks like paper. As the Paper UI also uses this card, it was decided to call it Paper UI.

### Why does it not support sitemaps?
 
Sitemaps require the Xtext DSL. The Paper UI aims to provide a full UI-only experience without any need for modifying configuration files. Thus the Paper UI can not make use of Sitemaps now, until it is refactored to have DSL support optional as it was done for items and things.
