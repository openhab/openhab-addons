angular.module('Zoo', [
	'Zoo.controllers',
	'ngRoute',
	'ngResource',
	'angularSpinner',
	'SmartHome.rest',
	'SmartHome.repositories',
	'SmartHome.services',
	'SmartHome.datacache'
]).config(['$routeProvider', '$locationProvider', 'usSpinnerConfigProvider', function ($routeProvider, $locationProvider, usSpinnerConfigProvider) {

	$locationProvider.html5Mode(false).hashPrefix('!');

	usSpinnerConfigProvider.setDefaults({color: 'blue'});

	$routeProvider.
		when('/login', {
			templateUrl: 'partials/login.html',
			controller: 'LoginController'
		})
		.when('/room', {
			templateUrl: 'partials/room.html'
			//controller: 'RoomController'
		})
		.when('/access', {
			templateUrl: 'partials/access.html'
		})
		.when('/alerts', {
			templateUrl: 'partials/alerts.html'
		})
		.when('/cctv', {
			templateUrl: 'partials/cctv.html'
		})
		.when('/cost-settings', {
			templateUrl: 'partials/cost-settings.html'
		})
		.when('/energy-center', {
			templateUrl: 'partials/energy-center.html'
		})
		.when('/profile', {
			templateUrl: 'partials/profile.html'
		})
		.when('/alarm', {
			templateUrl: 'partials/alarm.html'
		})
		.when('/profile', {
			templateUrl: 'partials/profile.html'
		})
		.when('/intercom', {
			templateUrl: 'partials/intercom.html'
		})
		.when('/discover', {
			templateUrl: 'partials/discover.html'
		})
		.when('/manual-setup', {
			templateUrl: 'partials/manual-setup.html'
		})
		.when('/groups', {
			templateUrl: 'partials/groups.html'
		})
		.otherwise({redirectTo: '/login'});

}]).run(['$location', '$rootScope', '$log', 'itemService', function ($location, $rootScope, $log, itemService) {

	$rootScope.$on('$routeChangeSuccess', function () {
		// Strip slash in front of current path:
		$rootScope.activeSection = $location.path().substr(1);
	});

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
	itemService.getByName({itemName:'gWeather'}, function(weatherItems) {
		console.log(weatherItems);
		weatherItems.members.forEach(function (item) {
			if (item.type === 'GroupItem') return;
			$rootScope.weather = {
				locationLabel: item.label,
				date: new Date(),
				temperature: item.state
			};
		});
	});




}]);