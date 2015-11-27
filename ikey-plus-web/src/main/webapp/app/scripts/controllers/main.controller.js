'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the webappApp
 */
angular.module('ikey')
    .controller('MainController', ['SystemInfoResource', function (SystemInfoResource) {
        var vm = this;
        SystemInfoResource.getServiceInfo().promise.then(function(serviceInfo){
            vm.serviceInfo = serviceInfo;
        });
        vm.awesomeThings = [
            'HTML5 Boilerplate',
            'AngularJS',
            'Karma'
        ];
    }]);
