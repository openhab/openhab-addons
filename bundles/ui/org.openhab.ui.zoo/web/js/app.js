angular.module('SmartHomeManagerApp', [
  'SmartHomeManagerApp.controllers',
  'SmartHomeManagerApp.controllers.control',
  'SmartHomeManagerApp.controllers.setup',
  'SmartHomeManagerApp.controllers.configuration',
  'SmartHomeManagerApp.services',
  'SmartHomeManagerApp.services.rest',
  'SmartHomeManagerApp.services.repositories',
  'ngRoute',
  'ngResource',
  'ngMaterial'
]).config(['$routeProvider', function($routeProvider) {
  $routeProvider.
	when('/control', {templateUrl: 'partials/control.html', controller: 'ControlPageController', title: 'Control', simpleHeader: true}).
	when('/setup', {redirectTo: '/setup/search'}).
	when('/setup/search', {templateUrl: 'partials/setup.html', controller: 'InboxController', title: 'Setup Wizard'}).
	when('/setup/manual-setup/choose', {templateUrl: 'partials/setup.html', controller: 'ManualSetupChooseController', title: 'Setup Wizard'}).
	when('/setup/manual-setup/configure/:thingTypeUID', {templateUrl: 'partials/setup.html', controller: 'ManualSetupConfigureController', title: 'Setup Wizard'}).
	when('/configuration', {redirectTo: '/configuration/bindings'}).
	when('/configuration/bindings', {templateUrl: 'partials/configuration.html', controller: 'BindingController', title: 'Configuration'}).
	when('/configuration/groups', {templateUrl: 'partials/configuration.html', controller: 'GroupController', title: 'Configuration'}).
	when('/configuration/things', {templateUrl: 'partials/configuration.html', controller: 'ThingController', title: 'Configuration'}).
	when('/configuration/things/view/:thingUID', {templateUrl: 'partials/configuration.html', controller: 'ViewThingController', title: 'Configuration'}).
	when('/configuration/things/edit/:thingUID', {templateUrl: 'partials/configuration.html', controller: 'EditThingController', title: 'Configuration'}).
	when('/preferences', {templateUrl: 'partials/preferences.html', controller: 'PreferencesPageController', title: 'Preferences'}).
	otherwise({redirectTo: '/control'});
}]).directive('editableitemstate', function(){
    return function($scope, $element) {
        $element.context.addEventListener('focusout', function(e){
            $scope.sendCommand($($element).html());
        });
    };
}).run(['$location', '$rootScope', '$mdToast', function($location, $rootScope, $route, $mdToast) {
	var original = $location.path;
	$rootScope.$on('$routeChangeSuccess', function (event, current, previous) {
        //$rootScope.title = current.$$route.title; // BAD! Never access via $$
        //$rootScope.simpleHeader = current.$$route.simpleHeader;
        $rootScope.path = $location.path().split('/');
        $rootScope.section = $rootScope.path[1];
        $rootScope.page = $rootScope.path[2];
    });
    $rootScope.asArray = function (object) {
        return $.isArray(object) ? object : object ? [ object ] : [] ;
    }

    $rootScope.data = [];
    $rootScope.navigateToRoot = function() {
        $location.path('');
    }
    $rootScope.navigateFromRoot = function(path) {
        $location.path(path);
    }
}]);