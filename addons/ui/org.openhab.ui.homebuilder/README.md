# Home Builder

> Boilerplate for the [Items](https://www.openhab.org/docs/configuration/items.html), [sitemap](https://www.openhab.org/docs/configuration/sitemaps.html) files and [HABPanel](https://www.openhab.org/docs/configuration/habpanel.html) dashboard.

## Usage

See [USAGE.md](USAGE.md) file for reference.

## Development

HomeBuilder UI is an app based on [Vue.js](http://vuejs.org) framework.
In order to setup the development environment simply run:

``` bash
cd web
npm install
npm run dev
```

The first command will install all necessary dependencies (including Webpack build system).
`npm run dev`, however, will serve the build with hot reload at `http://localhost:8080`.

It's recommended to debug the app on Chrome or Firefox with [Vue.js devtools](https://chrome.google.com/webstore/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd) extension.

When you're done with your changes and would like to test HomeBuilder on a real environment, run the following command:

``` bash
# build for production with minification
npm run build
```

It will build the app inside `/web/dist/` folder with source map included.
