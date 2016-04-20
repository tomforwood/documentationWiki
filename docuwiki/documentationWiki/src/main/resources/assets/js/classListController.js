angular.module('docuWikiApp').controller('classListCtrl', ['$scope', 'ClassList', function($scope, ClassList){
	$scope.classes = ClassList.query();
	$scope.expandedNamespaces = [];
	$scope.toggle=function(namespace){
		var index= $scope.expandedNamespaces.indexOf(namespace);
		if (index<0) $scope.expandedNamespaces.push(namespace);
		else $scope.expandedNamespaces.splice(index,1);
	}
}]);

docuWikiApp.filter('nameSpace', function() {
	return function(classDefs, query, subset, expandedNamespaces) {
		//This filter returns an array of entries for the classlist
		//consisting of [{namespaceName, classname},...]
		
		//namespace is undefined unless the 
		//namespace is a subnamespace of the previous value
		
		//if query is length>0 then all matching classnames are returned
		
		//it subset!=All then all matching classnames are retuned
		
		//else className is only defined for classes whose namespace
		//appears in expandedNamespaces
		
		//see tests in FilterSpec for more details
		var contains=function(array,value) {
			return array.indexOf(value)>=0;
		}
		
		var filtered = [];
		
		var len = classDefs.length;
		var prevNamespace = null;
		var queryRegExp = new RegExp(query,"i");
		
		for (var i=0;i<len;i++) {
			var classDef = classDefs[i];
			delete classDef.ns;
			delete classDef.cn;
			var suppressClass=false;
			
			
			//filter out classes that aren't going to have a row
			if ((query && query.length>0) || subset!="ALL") {
				//either we apply search type filters
				if (query && query.length>0) {
					//we are querying
					//ingore classes that dont match the query
					var match=classDef.className.search(queryRegExp);
					if (classDef.className.search(queryRegExp)<0) {
						continue;
					}
				}
				if (subset!="ALL") {
					if (subset=="DOCUMENTED" && !(classDef.subset & 2))
						continue;
					
					if (subset=="ORPHANED" && (classDef.subset & 1))
						continue;
				}
			}
			else {
				//or we apply namespace hiding filters
				if (classDef.namespace==prevNamespace &&
					classDef.namespace!=null &&
					!contains(expandedNamespaces,classDef.namespace)){
					//not the first entry in a suppressed namespace
					//so just ignore it
					continue;
				}
				if (classDef.namespace!=null && 
					!contains(expandedNamespaces,classDef.namespace)){
					//first entry in a suppressed namespace
					//return just the namespace name so you can click on it
					suppressClass=true;
				}
				if (prevNamespace!=null  && classDef.namespace!=null &&
						classDef.namespace.startsWith(prevNamespace) &&
						!contains(expandedNamespaces, prevNamespace)) {
					//if this namespace is a child of the previous ns
					//and that ns is suppressed then this one is skipped entirely
					continue;
				}
					
			}
			
			//var result={};
			var add=false;
			if (classDef.namespace!=null &&
					(prevNamespace==null ||
					!prevNamespace.startsWith(classDef.namespace))) {
				classDef.ns=classDef.namespace;
				add=true;
			}
			if (!suppressClass) {
				classDef.cn=classDef.className;
				add=true;
			}
			
			if (add) {
				prevNamespace = classDef.namespace;
				filtered.push(classDef);
			}
		}
		return filtered;
	};
});

docuWikiApp.filter('indenter', [function() {
	return function(input) {
		var strings= input.split(".");
		var out="";
		/*for (var i=0;i<strings.length-1;i++) {
			out=out+"&ndash;";
		}*/
		if (strings.length>1) {
			out=out+"&ndash;";
		}
		out = out+strings.pop();
		return out;
	};
}]);