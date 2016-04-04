grammar BasicCSharp;

compilationUnit : '\uFEFF'? region? 
	cuUsings=usings
	(namespace |
	firstClassThing);

region : RegionStart
	LINE_COMMENT* '#endregion';

RegionStart : '#region' ~[\r\n]* ;

usings : using*;

using : 'using' 
	useName = qualifiedName ';';

namespace :
	'namespace' 
	namespaceName = qualifiedName '{'
	firstClassThing '}';

firstClassThing: 
	classDeclaration | enumDeclaration | interfaceDeclaration | structDeclaration;

interfaceDeclaration :
	comment=docCommentBlock
	classmods=cmods
	'interface' name=identifier
	genArgs = typeArgs?
	extension?
	classBody;

classDeclaration :
	comment=docCommentBlock
	annotations
	classmods=cmods
	'class' name=identifier
	genArgs = typeArgs?
	extension?
	classBody;

docCommentBlock : DocComment*;
	
classOrInterfaceModifier : ('public'|'protected'| 'static' | 'sealed' | 'abstract' );

modifier: classOrInterfaceModifier  | 'virtual'| 
'ref'|'out'|'delegate'|'override'|'const'|'params'|'private'|'explicit';

modifiers : modifier*;

cmods : classOrInterfaceModifier*;

extension : ':' extended (',' extended)*;

extended: extName = type;

classBody : 
	'{'
	memberDeclaration*	
	'}';

memberDeclaration :
	(
	constructorDeclaration|
	methodDeclaration|
	fieldDeclaration|
	genericMethod|
	propertyDeclaration|
	enumDeclaration|
	classDeclaration|
	arrayLikeProperty| //I dont know what these are called in c#
	//but they are the things that let you go myList[1]
	structDeclaration
	);
	
fieldDeclaration : 
	comment = docCommentBlock
	annotations
	fieldMods = modifiers 
	fieldType=type 
	fieldName = identifier 
	fieldAssignment = assignment? ';';

methodDeclaration : 
	comment = docCommentBlock
	annotations
	methodMods = modifiers 
	methodType = type 
	methodName = identifier 
	methodParams = formalParams 
	(';'|propertyBody);

constructorDeclaration :
	comment = docCommentBlock
	annotations
	consMods = modifiers 
	identifier 
	consParams = formalParams ';';

propertyDeclaration :
	comment = docCommentBlock
	annotations
	propMods=modifiers 
	propType=type 
	propName=identifier propertyBody;
	
arrayLikeProperty :
	DocComment*
	annotations
	modifiers 
	type 'this['type identifier ']' propertyBody;
	
genericMethod : 
	DocComment*
	annotations
	modifiers 
	type identifier typeArgs formalParams 
	(WHERE identifier ':' type)? ';';
	
enumDeclaration :
	comment = docCommentBlock
	annotations
	enumMods = cmods
	'enum' 
	enumName = identifier 
	extension? 
	'{' (enumConstant)* '}';

enumConstant : 
	dockBlock = docCommentBlock 
	ident = identifier 
	('=' 
	intVal = IntLiteral)? ','?;

structDeclaration :
	comment = docCommentBlock 
	classmods=cmods
	'struct'
	name = identifier typeArgs? classBody ;

assignment : '=' literal;

formalParams : '(' 
	paramList=formalParamList 
	')';

formalParamList : formalParam (',' formalParam)* | ;

formalParam : modifiers 
	paramType = type 
	paramName = identifier ( '=' literal)?;

type:
	identifier 
	typeargs = typeArgs? 
	('.' identifier typeArgs? )* 
	('['']')*
	| primitiveType ('['']')*;

typeArgs:
	'<' type (','type)*'>';

qualifiedName :   identifier ('.' identifier)* ;

annotations :annotation*;

annotation : '[' identifier 
	( '(' ( elementValuePairs | literal )? ')' )? ']';

elementValuePairs
    :   elementValuePair (',' elementValuePair)*;
    
elementValuePair
    :   (Identifier '=' literal) | enumLiteral;

literal : StringLiteral | FPLiteral | IntLiteral | BoolLiteral | 'null' | enumLiteral;

enumLiteral : qualifiedName '.' identifier ('|' enumLiteral)*;

identifier:
	Identifier | WHERE;//grrr some keywords can be used as identifiers
	
propertyBody : '{' ( 
	propget='get;')? ( 
	propset='set;')? '}';

StringLiteral : '@'? '"' ~["]* '"';

FPLiteral : Digits (
	('.' Digits)('e' IntLiteral )?	[fF]? |//to be a float literal
		        ('e' IntLiteral ) [fF]? |//it must have either decimals
		        				[fFcD] //an exponent or a tag
	);

IntLiteral :  '-'? Digits ([lL])?;

BoolLiteral : 'true'|'false';

WHERE :'where';

fragment
Digits : Digit (Digit)*;

//these aren't a true part of the source - they have been added    
EXTERN : 'extern' -> skip ;

Identifier
    :   Letter LetterOrDigit* ;
    
fragment
Letter
    :   [a-zA-Z$_];

fragment
LetterOrDigit
    :   [a-zA-Z0-9$_];
    
    fragment
Digit
    :   [0-9]
    ;
    
primitiveType : 'bool'|'float'|'string'|'double'|'int'|'long'|'class'|'ulong';

WS  :  [ \t\r\n\u000C]+ -> skip
    ;
    
DocComment :
	'///' ~[\r\n]*;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//'~'/' ~[\r\n]* -> skip
    ;