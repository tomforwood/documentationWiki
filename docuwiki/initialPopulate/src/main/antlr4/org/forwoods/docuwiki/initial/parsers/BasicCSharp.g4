grammar BasicCSharp;

compilationUnit : '\uFEFF'? region 
	using+ 
	classDeclaration;

region : RegionStart
	LINE_COMMENT* '#endregion';

RegionStart : '#region' ~[\r\n]* ;

using : 'using' qualifiedName ';';

classDeclaration :
	comment=docCommentBlock
	classmods=classOrInterfaceModifier
	'class' name=Identifier
	classextends=extension?
	classBody;

docCommentBlock : DocComment*;
	
classOrInterfaceModifier : ('public'|'protected');
modifier: classOrInterfaceModifier  | 'static' | 'virtual'| 
'ref'|'out'|'delegate';

extension : ':' qualifiedName*;

classBody : 
	'{'
	memberDeclaration*	
	'}';

memberDeclaration :
	DocComment*
	annotation*
	modifier* 
	(
	constructorDeclaration|
	methodDeclaration|
	fieldDeclaration|
	genericMethod|
	propertyDeclaration|
	enumDeclaration);
	
fieldDeclaration :type Identifier ';';

methodDeclaration : type Identifier formalParams ';';

constructorDeclaration :
	Identifier formalParams ';';

propertyDeclaration :
	type Identifier IGNOREDBODY;
	
genericMethod : type Identifier typeArgs formalParams 
	('where' Identifier ':' type)? ';';
	
enumDeclaration :
	modifier*
	'enum' Identifier '{' (enumConstant)* '}';

enumConstant : DocComment* Identifier ('=' IntLiteral)? ',';

formalParams : '(' formalParamList ')';

formalParamList : formalParam (',' formalParam)* | ;

formalParam : modifier* type Identifier ( '=' literal)?;

type:
	Identifier typeArgs? ('.' Identifier typeArgs? )* ('['']')*
	| primitiveType ('['']')*;

typeArgs:
	'<' type (','type)*'>';

qualifiedName :   Identifier ('.' Identifier)* ;

annotation : '[' Identifier ( '(' StringLiteral ')')? ']';

literal : StringLiteral | FPLiteral | BoolLiteral | 'null';

StringLiteral : '"' ~["]* '"';

FPLiteral : Digits (
	('.' Digits) [fF]? |
		[fF]
	);

IntLiteral :  Digits ([lL])?;

BoolLiteral : 'true'|'false';

fragment
Digits : Digit (Digit)?;

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

//fragment
//SemiC : ';';
    
primitiveType : 'bool'|'float'|'string'|'double'|'int'|'long'|'class';

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

IGNOREDBODY : '{' ~[\r\n]* '}';