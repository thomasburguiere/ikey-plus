'use strict';

/**
 * @ngdoc overview
 * @name ikey
 * @description
 * # webappApp
 *
 * Main module of the application.
 */
angular
    .module('ikey', [
        'ngAnimate',
        'ngCookies',
        'ngResource',
        'ngRoute',
        'ngSanitize',
        'ngTouch',
        'ngFileUpload'
    ])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/main.html',
                controller: 'MainController',
                controllerAs: 'main'
            })
            .when('/about', {
                templateUrl: 'views/about.html',
                controller: 'AboutController',
                controllerAs: 'about'
            })
            .otherwise({
                redirectTo: '/'
            });
    }]);
