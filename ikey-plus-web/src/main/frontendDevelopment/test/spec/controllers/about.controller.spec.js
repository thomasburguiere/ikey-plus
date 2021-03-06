'use strict';

describe('Controller: AboutController', function () {

    // load the controller's module
    beforeEach(module('ikey'));

    var AboutCtrl,
        scope;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
        scope = $rootScope.$new();
        AboutCtrl = $controller('AboutController', {
            $scope: scope
            // place here mocked dependencies
        });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
        expect(AboutCtrl.awesomeThings.length).toBe(3);
    });
});
