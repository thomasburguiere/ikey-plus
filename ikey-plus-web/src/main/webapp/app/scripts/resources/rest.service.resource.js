'use strict';
angular.module('ikey')
    .service('SystemInfoService', ['$http', '$q', function ($http, $q) {
        this.getServiceInfo = function () {

            var deferred = $q.defer();

            $http({url: 'http://localhost:8080/ikey-rest/', method: 'GET'}).then(
                function (result) {
                    deferred.resolve({
                        version: result.data.version,
                        status: (result.status === 200) ? 'online' : 'offline'
                    });
                },
                function () { // error
                    deferred.resolve({version: 'unknown', status: 'offline'});
                });
            return deferred;
        };
    }]);