// rewiremock.cjs.js
const rewiremock = require('rewiremock/node');
/// settings
rewiremock.isolation();

//rewiremock.overrideEntryPoint(module); // this is important
module.exports = rewiremock;