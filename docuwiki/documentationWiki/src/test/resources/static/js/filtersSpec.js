describe('Filter tests', function(){
	beforeEach(module('docuWikiApp'));
	
	describe('markupifyFilter', function(){
		it('should convert c# cref links to html links', 
			inject(function(markupifyFilter) {
			expect(markupifyFilter('<see cref="CB"/>')).toBe('<a href="#/classes/CB">CB</a>');		
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
	});
	
	describe('indenterFilter', function(){
		it('should indent namespaced classes', 
			inject(function(indenterFilter) {
			expect(indenterFilter('A.B.C')).toBe('&ndash;C');		
		}));
	});
	
	describe('subsetFilter', function(){
		var classDefs=[{subset:3},{subset:1},{subset:2}];
		it('All returns all', 
			inject(function(subsetFilter) {
			expect(subsetFilter(classDefs,'ALL').length).toBe(3);
		}));
		it('Orphaned returns orphans', 
			inject(function(subsetFilter) {
			expect(subsetFilter(classDefs,'ORPHANED').length).toBe(1);
		}));
		it('Documented returns documented', 
			inject(function(subsetFilter) {
			expect(subsetFilter(classDefs,'DOCUMENTED').length).toBe(2);
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
					.toBe('List<'+classLinkFilterFilter('CB', classList)+'>');
		}));
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
						parameters:[{objectType:{typeName:'string'}},
						            {objectType:{typeName:'CB'}}]};
				var methodString = methodFilterFilter(method,classList);
				var objectLink = objectTypeFilter({typeName:'CB'},classList);
				expect(methodString).toBe('doStuff\(&#8203;string, '+objectLink+')');
		}));
	});
});