/* This file can be be overridden by fragments for customization */
angular.module('PaperUI.constants', [])
.constant('globalConfig', {
   'advancedDefault': true,
   'defaultRoute': '/inbox/search'
}).constant('restConfig', {
  'restPath': '/rest',
  'eventPath': $('#authentication').data('access-token') != '{{ACCESS_TOKEN}}' ? '/rest/events?access_token=' + $('#authentication').data('access-token') : '/rest/events'
}).constant('moduleConfig', {
  'control': true,
  'configuration': true,
  'setup': true,
  'configuration': true,
  'extensions' : {
      'label':'Add-ons',
      'visible' : true
  },
  'rules': true,
  'preferences': true,
  'groups': false
}).constant('moduleLabels', {
    'extensions' : 'Add-ons'
});
