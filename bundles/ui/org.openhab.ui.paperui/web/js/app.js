angular.module('SmartHomeManagerApp', [
  'SmartHomeManagerApp.controllers',
  'SmartHomeManagerApp.services',
  'ngRoute',
  'ngResource'
]).config(['$routeProvider', function($routeProvider) {
  $routeProvider.
	when('/control', {templateUrl: 'partials/control.html', controller: 'ControlPageController', title: 'Control',}).
	when('/control/:tab', {templateUrl: 'partials/control.html', controller: 'ControlPageController', title: 'Control',}).
	when('/inbox', {templateUrl: 'partials/inbox.html', controller: 'InboxPageController', title: 'Inbox',}).
	when('/inbox/:tab', {templateUrl: 'partials/inbox.html', controller: 'InboxPageController', title: 'Inbox',}).
	when('/configuration', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/:tab', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/:tab/:action', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/:tab/:action/:actionArg', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/preferences', {templateUrl: 'partials/preferences.html', controller: 'PreferencesPageController', title: 'Preferences'}).
	otherwise({redirectTo: '/control'});
}]).directive('slide', function(){
    return function($scope, $element){
        $element.context.addEventListener('change', function(e){
          $scope.setDimValue(e);
        });
      };
}).directive('colorslide', function(){
    return function($scope, $element){
        $element.context.addEventListener('change', function(e){
          $scope.setHueValue(e);
        });
      };
}).directive('switch', function(){
    return function($scope, $element){
        $scope.isOn() ? $($element).prop('selected', 'ON') : $($element).prop('selected', 'OFF'); 
    };
}).directive('tabs', function(){
    return function($scope, $element){
        $($element).prop('selected', $scope.tabs.indexOf($scope.currentTab)); 
    };
}).directive('editableitemstate', function(){
    return function($scope, $element) {
        $element.context.addEventListener('focusout', function(e){
            $scope.sendCommand($($element).html());
        });
    };
}).run(['$location', '$rootScope', function($location, $rootScope) {
    $rootScope.$on('$routeChangeSuccess', function (event, current, previous) {
        $rootScope.title = current.$$route.title;
    });
    $rootScope.asArray = function (object) {
        return $.isArray(object) ? object : object ? [ object ] : [] ;
    }
    $rootScope.toggleCard = function (event) {
        if($(event.target).parents('.card').find('.cbody').hasClass('hide')) {
            $rootScope.expandCard(event); 
        } else {
            $rootScope.collapseCard(event);
        }
    }
    $rootScope.expandCard = function (event) {
        $(event.target).parents('.card').find('.cbody').removeClass('hide');
        $(event.target).parents('.card').find('.expand-button').addClass('hide');
        $(event.target).parents('.card').find('.collapse-button').removeClass('hide');
    }
    $rootScope.collapseCard = function (event) {
        $(event.target).parents('.card').find('.cbody').addClass('hide');
        $(event.target).parents('.card').find('.expand-button').removeClass('hide');
        $(event.target).parents('.card').find('.collapse-button').addClass('hide');
    }
    $rootScope.data = [];
    $rootScope.navigateToRoot = function() {
        $location.path('');
    }
    $rootScope.showToast = function(id, text, actionText, actionUrl) {
        var selector = 'paper-toast#' + id + '-toast';
        var toast = $(selector);
        if(actionText) {
            toast.html('<a href="#'+actionUrl+'">'+actionText+'</a>');
            toast.prop('duration', 300000);
        } else {
            toast.html('');
            toast.prop('duration', 3000);
        }
        toast.prop('text', text);
        toast.prop('opened', true);
    }
    $rootScope.showDefaultToast = function(text, actionText, actionUrl) {
        $rootScope.showToast('default', text, actionText, actionUrl);
    }
    $rootScope.showErrorToast = function(text, actionText, actionUrl) {
        $rootScope.showToast('error', text, actionText, actionUrl);
    }
    $rootScope.showSuccessToast = function(text, actionText, actionUrl){
        $rootScope.showToast('success', text, actionText, actionUrl);
    }
}]);