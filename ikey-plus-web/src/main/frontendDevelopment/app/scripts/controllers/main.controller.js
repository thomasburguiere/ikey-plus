'use strict';

angular.module('ikey')
    .controller('MainController', ['SystemInfoResource', 'Upload', 'settings',
        function (SystemInfoResource, Upload, settings) {
            var vm = this;
            SystemInfoResource.getServiceInfo().then(function (serviceInfo) {
                vm.serviceInfo = serviceInfo;
            });

            vm.submit = function (file) {
                file.upload = Upload.upload({
                    url: settings.REST_API + 'upload/',
                    data: {file: file}
                });


                file.upload.then(function (success) {
                    console.log('success', success);
                }, function (error) {
                    console.log('error', error);
                });
            };


            vm.awesomeThings = [
                'HTML5 Boilerplate',
                'AngularJS',
                'Karma'
            ];
        }]);
