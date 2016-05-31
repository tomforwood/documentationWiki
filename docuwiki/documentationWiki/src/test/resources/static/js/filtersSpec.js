describe('Filter tests', function(){
	beforeEach(module('docuWikiApp'));
	
	describe('markupifyFilter', function(){
		it('should convert c# cref links to html links', 
			inject(function(markupifyFilter) {
			expect(markupifyFilter('<see cref="CB"/>')).toBe('<a href="#/classes/CB">CB</a>');		
		}));
		
		it('converts params to nice html', 
			inject(function(markupifyFilter) {
			expect(markupifyFilter('<param name="obj">The object </param>'))
				.toBe('<br>param obj &ndash; The object ');		
		}));
		
		it('converts params to nice html', 
				inject(function(markupifyFilter) {
				expect(markupifyFilter("<param name='r'>The</param><param name='r'>The</param>"))
				.toBe('<br>param r &ndash; The<br>param r &ndash; The');		
				}));
		
		it('converts returns to nice html', 
			inject(function(markupifyFilter) {
			expect(markupifyFilter('<returns>A ConfigNode.</returns>'))
				.toBe('<br>returns &ndash; A ConfigNode.');		
		}));

		it('links should match that created by the classLink filter', 
			inject(function(markupifyFilter, classLinkFilterFilter) {
			expect(markupifyFilter('<see cref="CB"/>')).toBe(classLinkFilterFilter('CB',[{className:'CB'}]));		
		}));
	});
	
	describe('ClassLinkFilter', function(){
		it('should just return class name for unknown', 
			inject(function(classLinkFilterFilter) {
			expect(classLinkFilterFilter('CB',[])).toBe('CB');		
		}));

		it('should return link for existing class', 
				inject(function(classLinkFilterFilter) {
				expect(classLinkFilterFilter('CB',[{className:'CB'}])).toBe('<a href="#/classes/CB">CB</a>');			
		}));

		it('should return anchor link for nested existing class', 
				inject(function(classLinkFilterFilter) {
				expect(classLinkFilterFilter('CB+Fish',[{className:'CB'}])).toBe('<a href="#/classes/CB?scrollTo=FishN">CB.Fish</a>');			
		}));
		
		it('should create links for arrays',
				inject(function(classLinkFilterFilter){
					expect(classLinkFilterFilter('CB[]',[{className:'CB'}])).toBe('<a href="#/classes/CB">CB[]</a>');
		}));
	});
	
	describe('indenterFilter', function(){
		it('should indent namespaced classes', 
			inject(function(indenterFilter) {
			expect(indenterFilter('A.B.C')).toBe('&ndash;C');		
		}));
	});
	
	describe('nameSpaceFilter', function(){
		beforeEach(function() {
			jasmine.addMatchers({
				toEqualData : function() {
					return {
						compare:function(actual, expected) {
							//works on two arrays
							//checks that for each property set in each object 
							//in the expected array the actual has the same value
							if (actual.length!=expected.length) return {pass:false};
							for (var i=0;i<expected.length;i++) {
								acItem = actual[i];
								exItem = expected[i];
								for (var prop in exItem) {
									if (acItem[prop]!=exItem[prop]) return {pass:false};
								}
							}
							
							return {pass:true};
						}
					};
				}
			});
		});
		
		var classList;
		
		beforeEach(function(){
			classList=[{namespace:null,className:"Class1","subset":3},
		               {namespace:"A",className:"A.Class2","subset":2},
		               {namespace:"A",className:"A.Class3","subset":1},
		               {namespace:"A.B",className:"A.B.Class3.1","subset":1},
		               {namespace:null,className:"Class4","subset":1},
		               {namespace:null,className:"Class5","subset":2},
		               {namespace:"C.D",className:"C.D.Class6","subset":2}];
		});
		
		it('Collapses namespaces', inject(function(nameSpaceFilter){
			expect(nameSpaceFilter(classList, null, "ALL", []))
				.toEqualData([{cn:"Class1"},{ns:"A"},{cn:"Class4"},{cn:"Class5"},{ns:"C.D"}]);
		}));
		
		it('Can expand namespaces', inject(function(nameSpaceFilter){
			var expanded=["A","C.D"];
			expect(nameSpaceFilter(classList, null, "ALL", expanded))
				.toEqualData([{cn:"Class1"},{ns:"A",cn:"A.Class2"},{cn:"A.Class3"},
					{ns:"A.B"},{cn:"Class4"},{cn:"Class5"},{ns:"C.D",cn:"C.D.Class6"}]);
		}));
		it('Copes with namespace first', inject(function(nameSpaceFilter){
			var item0={namespace:"Z",className:"Class0"}
			classList.unshift(item0);
			expect(nameSpaceFilter(classList, null, "ALL", []))
			.toEqualData([{ns:"Z"},{cn:"Class1"},{ns:"A"},{cn:"Class4"},{cn:"Class5"},{ns:"C.D"}]);
		}));
		
		it('Can query', inject(function(nameSpaceFilter){
			var expanded=["C.D"];
			var query = "3";
			expect(nameSpaceFilter(classList, query, "ALL", expanded))
				.toEqualData([{ns:"A",cn:"A.Class3"},{ns:"A.B",cn:"A.B.Class3.1"}]);
		}));
		
		it('Can Subset', inject(function(nameSpaceFilter){
			var expanded=["C.D"];
			var query = "";
			subset="DOCUMENTED";
			expect(nameSpaceFilter(classList, query, subset, expanded))
				.toEqualData([{cn:"Class1"},{ns:"A",cn:"A.Class2"},
				{cn:"Class5"},{ns:"C.D",cn:"C.D.Class6"}]);
		}));
		
		it('Can Subset AND filter', inject(function(nameSpaceFilter){
			var expanded=["C.D"];
			var query = "A.C";
			subset="ORPHANED";
			expect(nameSpaceFilter(classList, query, subset, expanded))
				.toEqualData([{ns:"A",cn:"A.Class2"}]);
		}));
	});
	
	describe('ObjectTypeFilter', function(){
		var classList=[{className:'CB'}];
		it('creates correct links', 
			inject(function(objectTypeFilter, classLinkFilterFilter) {
			expect(objectTypeFilter({typeName:'CB'},classList))
			.toBe(classLinkFilterFilter('CB', classList));
		}));
		it('formats varargs with links', 
				inject(function(objectTypeFilter, classLinkFilterFilter) {
				expect(objectTypeFilter({typeName:'List',varargs:[{typeName:'CB'}]},classList))
					.toBe('List&lt;'+classLinkFilterFilter('CB', classList)+'&gt;');
		}));
		it("formats Complicated thing", function(){
			inject(function(objectTypeFilter, classLinkFilterFilter) {
				var objectType={"typeName":"EventData","varargs":[{"typeName":"float"},{"typeName":"ScienceSubject"},{"typeName":"ProtoVessel"},{"typeName":"bool"}]};
				expect(objectTypeFilter(objectType,classList))
				.toBe("EventData&lt;float,ScienceSubject,ProtoVessel,bool&gt;");
			});
		});
	});
	
	describe('InheritedFilter', function(){
		var classList=[{className:'CB'}];
		it('creates correct links', 
			inject(function(inhertitedFilterFilter, classLinkFilterFilter) {
				//this filter isn't actually used for N(ested) links but
				//it must generate the same links as classLinkFilter does for nested classes
				var resultHref = inhertitedFilterFilter({inheritedFrom:'CB',name:'Method'},'N',classList)
					.replace(/^.*href="([^"]*)".*$/,'$1');
				var expectedHref = classLinkFilterFilter('CB+Method', classList)
					.replace(/^.*href="([^"]*)".*$/,'$1');
				expect(resultHref).toBe(expectedHref);
		}));
	});
	
	describe('methodFilter', function(){
		var classList=[{className:'CB'}];
		it('formats methods', 
			inject(function(methodFilterFilter, objectTypeFilter) {
				var method = {name:'doStuff',
						parameters:[{objectType:{typeName:'string'}, name:'param1'},
						            {objectType:{typeName:'CB'}, name:'param2'}]};
				var methodString = methodFilterFilter(method,classList);
				var objectLink = objectTypeFilter({typeName:'CB'},classList);
				expect(methodString).toBe('doStuff\(<wbr>string&nbsp;param1, '+objectLink+'&nbsp;param2)');
		}));
	});
});