angular.module('ZooApp', [
	'ui.router',
	'ngResource',
	'angularSpinner',
	'SmartHome.services',
	'ZooLib.controllers'
]).config(function ($stateProvider, $urlRouterProvider, usSpinnerConfigProvider) {

	//$locationProvider.html5Mode(false).hashPrefix('!');
	$urlRouterProvider.otherwise("/login");

	usSpinnerConfigProvider.setDefaults({color: 'blue'});

	$stateProvider
		.state('login', {
			url:'/login',
			templateUrl: 'partials/login.html',
			controller: 'LoginController as ctrl'
		})
		.state('room', {
			url:'/room',
			templateUrl: 'partials/room.html',
			controller: 'RoomController as ctrl'
		})
		.state('access', {
			url:'/access',
			templateUrl: 'partials/access.html'
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
			url:'/alarm',
			templateUrl: 'partials/alarm.html'
		})
		.state('settings', {
			templateUrl: 'partials/settings.html'
		})
		.state('settings.discover', {
			templateUrl: 'partials/settings.discover.html'
		})
		.state('settings.manual', {
			templateUrl: 'partials/settings.manual.html'
		})
		.state('settings.groups', {
			templateUrl: 'partials/settings.groups.html'
		})

}).run(['$location', '$rootScope', '$log', 'itemService', function ($location, $rootScope, $log, itemService) {

	// TODO Get rid of this
	$rootScope.data = {};
	
	$rootScope.leftSidebarOpen = false;
	$rootScope.isBlackout = false;

	$rootScope.toggleSidebar = function () {
		$rootScope.leftSidebarOpen = !$rootScope.leftSidebarOpen;
		$rootScope.isBlackout = !$rootScope.isBlackout;
	};

	itemService.getByName({itemName:'gRooms'}, function (rooms) {
		$rootScope.smarthome = {rooms : rooms.members};
	});

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