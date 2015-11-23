'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the webappApp
 */
angular.module('ikey')
    .controller('MainController', function ($http) {
        var isServiceOnline = $http({url: 'http://localhost:8080/ikey-rest/', method:'GET'}).then(function(result){
            console.log(result);
        });
        this.awesomeThings = [
            'HTML5 Boilerplate',
            'AngularJS',
            'Karma'
        ];
    });
