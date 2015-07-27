'use strict';

angular.module('ZooApp', [
	'ui.router',
	'ngResource',
	'angularSpinner',
	'pikaday',
	'SmartHome.services',
	'SmartHome.filters',
	'ZooLib.services',
	'ZooLib.controllers',
	'ZooLib.directives'
]).config(function ($stateProvider, $urlRouterProvider, usSpinnerConfigProvider) {

	//$locationProvider.html5Mode(false).hashPrefix('!');
	$urlRouterProvider.otherwise("/login");
	$urlRouterProvider.when('/rooms', '/rooms/');
	$urlRouterProvider.when('/settings', '/settings/');

	usSpinnerConfigProvider.setDefaults({color: 'blue'});

	$stateProvider
		.state('login', {
			url: '/login',
			templateUrl: 'partials/login.html',
			controller: 'LoginController as ctrl'
		})
		.state('rooms', {
			abstract: true,
			url: '/rooms',
			templateUrl: 'partials/rooms.html',
			controller: 'RoomController as ctrl'
		})
		.state('rooms.room', {
			url: '/:room',
			templateUrl: 'partials/rooms.devices.html'
		})
		.state('rooms.scenes', {
			url: '/:room/:scenes',
			templateUrl: 'partials/rooms.scenes.html'
		})
		.state('access', {
			url:'/access',
			templateUrl: 'partials/access.html',
			controller: 'AccessController as ctrl'
		})
		.state('alerts', {
			url:'/alerts',
			templateUrl: 'partials/alerts.html'
		})
		.state('cctv', {
			url:'/cctv',
			templateUrl: 'partials/cctv.html'
		})
		.state('cost-settings', {
			url:'/cost-settings',
			templateUrl: 'partials/cost-settings.html'
		})
		.state('energy-center', {
			url:'/energy-center',
			templateUrl: 'partials/cost-settings.html'
		})
		.state('profile', {
			url:'profile',
			templateUrl: 'partials/profile.html'
		})
		.state('alarm', {
			url:'/alarm',
			templateUrl: 'partials/alarm.html'
		})
		.state('intercom', {
			url:'/intercom',
			templateUrl: 'partials/intercom.html'
		})
		.state('settings', {
			abstract: true,
			url: '/settings',
			templateUrl: 'partials/settings.html',
			controller: 'SettingsController as ctrl'
		})
		.state('settings.discover', {
			url: '/discover',
			templateUrl: 'partials/settings.discover.html'
		})
		.state('settings.manual', {
			url: '/manual',
			templateUrl: 'partials/settings.manual.html'
		})
		.state('settings.groups', {
			url: '/groups',
			templateUrl: 'partials/settings.groups.html'
		})

}).run(['$location', '$rootScope', '$log', 'itemService', function ($rootScope) {



	//itemService.getByName({itemName:'gRooms'}, function (rooms) {
	//	$rootScope.smarthome = {rooms : rooms.members};
	//});

	// TODO Move to directive
	//itemService.getByName({itemName:'gWeather'}, function(weatherItems) {
	//	console.log(weatherItems);
	//	weatherItems.members.forEach(function (item) {
	//		if (item.type === 'GroupItem') return;
	//		$rootScope.weather = {
	//			locationLabel: item.label,
	//			date: new Date(),
	//			temperature: item.state
	//		};
	//	});
	//});
}]);

angular.module("ZooApp").run(function ($rootScope, $state, $stateParams) {
	$rootScope.$state = $state;
	$rootScope.$stateParams = $stateParams;
});

angular.module("ZooApp").run(function ($rootScope, eventService, $log) {
	eventService.onEvent('smarthome/*', function(topic, newState) {
		$log.debug('Received Event', topic, newState);
		$log.debug('Received state',newState);
		//split itemName and broadcast
		var itemName = topic.split('/')[2];

		var eventState = newState.value;
		//console.log(eventState);
		// this is for restful
		$rootScope.$broadcast('item', newState.value);
		// this is tailored for this UI
		$rootScope.$broadcast(itemName,eventState);
	});

});

// TODO Remove this, only for dev purposes
angular.module("ZooApp").run(function ($rootScope) {
	$rootScope.leftSidebarOpen = false;
    $rootScope.isBlackout = false;
    $rootScope.console = {}
    $rootScope.console.log = function(content){
      console.log(content);
    };

    $rootScope.toggleSidebar = function () {
      console.log($rootScope.leftSidebarOpen);
      $rootScope.leftSidebarOpen = !$rootScope.leftSidebarOpen;
      $rootScope.isBlackout = !$rootScope.isBlackout;
    };

	$rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
		console.log('state changed: from "%s" to "%s"', fromState.name, toState.name);
	
		if (toState.name === 'login') {
			$rootScope.user = {};
		} else {
			$rootScope.user = {name: 'Mr. Johnson', houseId: 1, isAuthenticated: true};
		}
	})
});
