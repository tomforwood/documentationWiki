describe("ClassViewCtl", function(){
	beforeEach(module('docuWikiApp'));
	beforeEach(module('docuWikiServices'));
	

	beforeEach(function() {
		jasmine.addMatchers({
			toEqualData : function() {
				return {
					compare:function(actual, expected) {
						// Use angular equals to ignore angular magic
						// stuff in returned objects
						result = {};
						result.pass = angular.equals(actual, expected);
						return result;
					}
				};
			}
		});
	});
	
	var scope, httpBackend,createController, stateParams;
	
	beforeEach(inject(function($rootScope, $controller, $stateParams, $httpBackend) {
		scope = $rootScope.$new();
		httpBackend = $httpBackend;
		stateParams = $stateParams;
		createController = function() {
			return $controller('classViewCtrl',{$scope:scope});
		};
	}));
	
	afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });
	
	var classDef = {name:'CB'};
		
	it("loads a class",function() {
		stateParams.classname='CB';
		var controller = createController();
		httpBackend.expectGET('api/class/CB').respond(classDef)
		httpBackend.expectGET('api/classList').respond([{name:'CB'}, {name:'AD'}])
		
		expect(scope.classFQN).toBe('CB');
		expect(scope.mergedClass).toEqualData({});
		httpBackend.flush();
		expect(scope.mergedClass).toEqualData(classDef);
		expect(scope.classList).toEqualData([{name:'CB'},{name:'AD'}]);
		//expect(scope.PostDataResponse).toBe('Error');
	});
	
	it("can save a class", function(){
		stateParams.classname='CB';
		var controller = createController();
		//gets on controller init
		httpBackend.expectGET('api/class/CB').respond(classDef);
		httpBackend.expectGET('api/classList').respond([{name:'CB'}, {name:'AD'}])
		httpBackend.flush();
		
		//post to save data
		//followed by fetch of new data
		httpBackend.expectPOST('api/class',classDef).respond(classDef);
		httpBackend.expectGET('api/class/CB').respond(classDef);//query for newest version
		
		scope.save();
		httpBackend.flush();
		
	});
});