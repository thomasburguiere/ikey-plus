'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the webappApp
 */
angular.module('ikey')
    .controller('MainController', ['SystemInfoService', function (SystemInfoService) {
        var vm = this;
        SystemInfoService.getServiceInfo().promise.then(function(serviceInfo){
            vm.serviceInfo = serviceInfo;
        });
        vm.awesomeThings = [
            'HTML5 Boilerplate',
            'AngularJS',
            'Karma'
        ];
    }]);
