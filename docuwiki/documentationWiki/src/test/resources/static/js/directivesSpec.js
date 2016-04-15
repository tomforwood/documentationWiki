describe('Method directive', function(){
	beforeEach(module('docuWikiApp'));
	var $compile,
    $rootScope;
	
	beforeEach(inject(function(_$compile_, _$rootScope_){
	    $compile = _$compile_;
	    $rootScope = _$rootScope_;
	}));
	
	/*it ('outputs a method description', function(){
		$rootScope.method={name:"fish"};
		$rootScope.classList=[{name:'CB'}, {name:'AD'}];
		var element = $compile('<tr method-element="method" class-list="classList"></tr>')($rootScope);
		$rootScope.$digest();
		expect(element.html()).toContain('fish');
	});*/
	  
});