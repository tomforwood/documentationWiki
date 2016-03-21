grammar BasicCSharp;

compilationUnit : '\uFEFF'? region? 
	using*
	(namespace |
	firstClassThing);

region : RegionStart
	LINE_COMMENT* '#endregion';

RegionStart : '#region' ~[\r\n]* ;

using : 'using' qualifiedName ';';

namespace :
	'namespace' qualifiedName '{'
	firstClassThing '}';

firstClassThing: 
	classDeclaration | enumDeclaration | interfaceDeclaration | structDeclaration;

interfaceDeclaration :
	comment=docCommentBlock
	classmods=classOrInterfaceModifier*
	'interface' name=identifier
	genArgs = typeArgs?
	classextends=extension?
	classBody;

classDeclaration :
	comment=docCommentBlock
	annotation*
	classmods=classOrInterfaceModifier*
	'class' name=identifier
	genArgs = typeArgs?
	classextends=extension?
	classBody;

docCommentBlock : DocComment*;
	
classOrInterfaceModifier : ('public'|'protected'| 'static' | 'sealed' );

modifier: classOrInterfaceModifier  | 'virtual'| 
'ref'|'out'|'delegate'|'override'|'const'|'params'|'private'|'explicit';

extension : ':' type (',' type)*;

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
	DocComment*
	annotation*
	modifier* 
	type identifier 
	assignment? ';';

methodDeclaration : 
	DocComment*
	annotation*
	modifier* 
	type identifier formalParams (';'|propertyBody);

constructorDeclaration :
	DocComment*
	annotation*
	modifier* 
	identifier formalParams ';';

propertyDeclaration :
	DocComment*
	annotation*
	modifier* 
	type identifier propertyBody;
	
arrayLikeProperty :
	DocComment*
	annotation*
	modifier* 
	type 'this['type identifier ']' propertyBody;
	
genericMethod : 
	DocComment*
	annotation*
	modifier* 
	type identifier typeArgs formalParams 
	(WHERE identifier ':' type)? ';';
	
enumDeclaration :
	DocComment*
	annotation*
	modifier*
	'enum' identifier extension? '{' (enumConstant)* '}';

enumConstant : DocComment* identifier ('=' IntLiteral)? ','?;

structDeclaration :
	DocComment*
	modifier*
	'struct'
	identifier typeArgs? classBody ;

assignment : '=' literal;

formalParams : '(' formalParamList ')';

formalParamList : formalParam (',' formalParam)* | ;

formalParam : modifier* type identifier ( '=' literal)?;

type:
	identifier typeArgs? ('.' identifier typeArgs? )* ('['']')*
	| primitiveType ('['']')*;

typeArgs:
	'<' type (','type)*'>';

qualifiedName :   identifier ('.' identifier)* ;

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
	
propertyBody : '{' (modifier? 'get;')? (modifier? 'set;')? '}';

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