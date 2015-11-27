'use strict';

describe('Controller: MainController', function () {

    // load the controller's module
    beforeEach(module('ikey'));

    var controller,
        scope,
        SystemInfoServiceMock = {
            getServiceInfo: function () {
                var defer = $q.defer();
                defer.resolve({
                    version: 'dummyVersion',
                    status: 'dummyStatus'
                });
                return defer.promise;
            }
        },
        $rootScope,
        $q,
        $httpBackend;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, _$rootScope_, _$q_, _$httpBackend_) {
        $rootScope = _$rootScope_;
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        scope = $rootScope.$new();
        controller = $controller('MainController', {
            $scope: scope,
            // place here mocked dependencies
            SystemInfoService: SystemInfoServiceMock
        });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
        expect(controller.awesomeThings.length).toBe(3);
    });


    it('should attach serviceInfo to the scope', function () {
        //when
        $rootScope.$apply();

        //then
        expect(controller.serviceInfo).toBeDefined();
        expect(controller.serviceInfo.version).toBe('dummyVersion');
    });
});
