grammar Javamm;

@header {
    package pt.up.fe.comp2026;
}

CLASS : 'class' ;
EXTENDS : 'extends';
INT : 'int' ;
BOOLEAN : 'boolean' ;
VOID: 'void';
STATIC : 'static' ;
RETURN : 'return' ;
PACKAGE: 'package';
IMPORT: 'import';
PUBLIC: 'public';
PRIVATE: 'private';
PROTECTED: 'protected';
THIS: 'this';
NEW: 'new';
LENGTH: 'length';

IF:'if';
ELSE:'else';

WHILE: 'while';
FOR: 'for';
DO: 'do';

INTEGER : '0' | [1-9][0-9]* ;
BOOL: 'true' | 'false';
ID : [a-zA-Z$_][a-zA-Z0-9$_]* ;

WS : [ \t\n\r\f]+ -> skip ;
SINGLE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;

program
    : packageDecl importDecl* classNode=classDecl EOF
    ;

stmtEntry
    : stmt EOF
    ;

expression
    : expr EOF
    ;

importDecl
    : IMPORT path+=ID '.' path+=ID ('.' path+=ID)* ';'
    ;

packageDecl
    : PACKAGE path += ID ('.' path +=ID)* ';'
    ;

classDecl
    : CLASS name=ID (EXTENDS superName=ID)?
        '{'
        classMember*
        '}'
    ;

classMember
    : fieldDecl
    | methodDecl
    ;

fieldDecl
    : typeNode=type name=ID ('=' expr)? ';'
    ;

varDecl
    : typeNode = type name=ID ';'
    ;

param
    : typeNode=type name=ID
    ;

type
    : (name=INT
      | name=BOOLEAN
      | name=VOID
      | name=ID)
      (dims+='[' dims+=']')*
    ;

methodDecl locals[boolean isStatic=false]
    : visibility=(PUBLIC | PRIVATE | PROTECTED)? (STATIC {$isStatic=true;})?
        returnType=type name=ID
        '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'
    ;

assignment
    : name=ID '=' expr
    ;

stmt
    : '{' stmt* '}'                                                              #Block
    | FOR '(' forInit? ';' forCond? ';' forUpdate? ')' stmt                    #ForStmt
    | WHILE '(' expr ')' stmt (ELSE stmt)?                                      #WhileStmt
    | DO stmt WHILE '(' expr ')' ';'                                            #DoWhileStmt
    | IF '(' expr ')' stmt ELSE stmt                                            #IfElseStmt
    | IF '(' expr ')' stmt                                                      #IfStmt
    | var=ID '=' expr ';'                                                       #AssignStmt
    | name=ID '[' expr ']' ('[' expr ']')* '=' expr ';'                        #ArrayStoreStmt
    | RETURN expr? ';'                                                          #ReturnStmt
    | expr ';'                                                                  #ExprStmt
    ;

forInit   : assignment ;
forCond   : expr ;
forUpdate : assignment ;

expr
    : '(' expr ')'                                              #ParenExpr
    | expr '.' LENGTH                                           #LengthExpr
    | expr '.' name=ID '(' (expr (',' expr)*)? ')'             #MethodCallExpr
    | expr '.' name=ID                                         #FieldAccessExpr
    | name=ID '(' (expr (',' expr)*)? ')'                      #ImplicitThisCallExpr
    | expr '[' expr ']'                                        #ArrayLoadExpr
    | THIS                                                     #ThisExpr
    | NEW name=ID '(' (expr (',' expr)*)? ')'                  #NewExpr
    | NEW INT '[' ']' '{' (expr (',' expr)*)? '}'              #ArrayInitializer
    | NEW INT '[' expr ']' ('[' expr edims+=']')* (edims+='[' ']')*  #NewArrayExpr
    | op='++' expr                                             #PlusPlusExpr
    | op='--' expr                                             #MinusMinusExpr
    | op='+' expr                                              #PlusExpr
    | op='-' expr                                              #MinusExpr
    | op='!' expr                                              #UnaryExpr
    | expr op='*' expr                                         #BinaryExpr
    | expr op='/' expr                                         #BinaryExpr
    | expr op='%' expr                                         #BinaryExpr
    | expr op='+' expr                                         #BinaryExpr
    | expr op='-' expr                                         #BinaryExpr
    | expr op='<' expr                                         #BinaryExpr
    | expr op='>' expr                                         #BinaryExpr
    | expr op='<=' expr                                        #BinaryExpr
    | expr op='>=' expr                                        #BinaryExpr
    | expr op='==' expr                                        #BinaryExpr
    | expr op='!=' expr                                        #BinaryExpr
    | expr op='&&' expr                                        #BinaryExpr
    | expr op='||' expr                                        #BinaryExpr
    | value=INTEGER                                            #IntegerLiteral
    | value=BOOL                                               #BoolLiteral
    | name=ID                                                  #VarRefExpr
    ;
