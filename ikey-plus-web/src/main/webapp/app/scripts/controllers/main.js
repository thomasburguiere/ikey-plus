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
        $http({url: 'http://localhost:8080/ikey-rest/', method: 'GET'}).then(function (result) {
            vm.serviceInfo = {version: result.data.version, status: (result.status === 200) ? 'online':'offline'};
        });
        var vm = this;
        //vm.
        vm.awesomeThings = [
            'HTML5 Boilerplate',
            'AngularJS',
            'Karma'
        ];
    });
