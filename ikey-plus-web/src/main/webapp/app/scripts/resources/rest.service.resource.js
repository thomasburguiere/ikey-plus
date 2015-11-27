'use strict';
angular.module('ikey')
    .factory('SystemInfoResource', ['$http', '$q', 'settings', function ($http, $q, settings) {
        return {
            getServiceInfo: function () {

                var deferred = $q.defer();

                $http({url: settings.REST_API, method: 'GET'}).then(
                    function (success) {
                        deferred.resolve({
                            version: success.data.version,
                            status: (success.status === 200) ? 'online' : 'offline'
                        });
                    },
                    function () { // error
                        deferred.resolve({
                            version: 'unknown',
                            status: 'offline'
                        });
                    });
                return deferred.promise;
            }
        };

    }]);
