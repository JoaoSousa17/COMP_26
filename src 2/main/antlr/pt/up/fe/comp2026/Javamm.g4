grammar Javamm;

@header {
    package pt.up.fe.comp2026;
}

// ─── Keywords ───────────────────────────────────────────────────────────────
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

// ─── Literals & Identifiers ──────────────────────────────────────────────────
INTEGER : '0' | [1-9][0-9]* ;
BOOL: 'true' | 'false';
ID : [a-zA-Z$_][a-zA-Z0-9$_]* ;

// ─── Whitespace & Comments (skipped) ────────────────────────────────────────
WS : [ \t\n\r\f]+ -> skip ;
SINGLE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;

// ─── Top-level entry points ──────────────────────────────────────────────────

/** Full Java-- program: optional package, imports, and exactly one class declaration. */
program
    : packageDecl importDecl* classNode=classDecl EOF
    ;

/** Entry point for parsing a single statement (used in tests/tooling). */
stmtEntry
    : stmt EOF
    ;

/** Entry point for parsing a single expression (used in tests/tooling). */
expression
    : expr EOF
    ;

// ─── Declarations ────────────────────────────────────────────────────────────

/** Import declaration: requires at least two path segments (e.g. import a.b.C). */
importDecl
    : IMPORT path+=ID '.' path+=ID ('.' path+=ID)* ';'
    ;

/** Package declaration: one or more dot-separated identifiers. */
packageDecl
    : PACKAGE path += ID ('.' path +=ID)* ';'
    ;

/** Class declaration with an optional superclass. Body contains fields and methods. */
classDecl
    : CLASS name=ID (EXTENDS superName=ID)?
        '{'
        classMember*
        '}'
    ;

/** A class member is either a field or a method declaration. */
classMember
    : fieldDecl
    | methodDecl
    ;

/** Field declaration at class level, with an optional initializer expression. */
fieldDecl
    : typeNode=type name=ID ('=' expr)? ';'
    ;

/** Local variable declaration inside a method body (no initializer). */
varDecl
    : typeNode = type name=ID ';'
    ;

/** A single method parameter: type followed by name. */
param
    : typeNode=type name=ID
    ;

/**
 * Type rule: covers primitives (int, boolean, void) and class types (ID).
 * Zero or more array dimension pairs '[]' may follow, supporting multi-dimensional arrays.
 */
type
    : (name=INT
      | name=BOOLEAN
      | name=VOID
      | name=ID)
      (dims+='[' dims+=']')*
    ;

/**
 * Method declaration with optional visibility modifier and static flag.
 * Body consists of local variable declarations followed by statements.
 */
methodDecl locals[boolean isStatic=false]
    : visibility=(PUBLIC | PRIVATE | PROTECTED)? (STATIC {$isStatic=true;})?
        returnType=type name=ID
        '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'
    ;

/** Simple assignment used as a for-loop initializer or update clause. */
assignment
    : name=ID '=' expr
    ;

// ─── Statements ──────────────────────────────────────────────────────────────

stmt
    : '{' stmt* '}'                                                              #Block
    | FOR '(' forInit? ';' forCond? ';' forUpdate? ')' stmt                    #ForStmt
    | WHILE '(' expr ')' stmt (ELSE stmt)?                                      #WhileStmt  // non-standard while-else
    | DO stmt WHILE '(' expr ')' ';'                                            #DoWhileStmt
    | IF '(' expr ')' stmt ELSE stmt                                            #IfElseStmt
    | IF '(' expr ')' stmt                                                      #IfStmt
    | var=ID '=' expr ';'                                                       #AssignStmt
    | name=ID '[' expr ']' ('[' expr ']')* '=' expr ';'                        #ArrayStoreStmt // supports multi-dimensional store
    | RETURN expr? ';'                                                          #ReturnStmt
    | expr ';'                                                                  #ExprStmt
    ;

/** For-loop sub-rules, kept separate to allow optional clauses in the for header. */
forInit   : assignment ;
forCond   : expr ;
forUpdate : assignment ;

// ─── Expressions (precedence: highest to lowest) ─────────────────────────────

expr
    : '(' expr ')'                                              #ParenExpr
    | expr '.' LENGTH                                           #LengthExpr             // array .length
    | expr '.' name=ID '(' (expr (',' expr)*)? ')'             #MethodCallExpr          // instance method call
    | expr '.' name=ID                                         #FieldAccessExpr         // field access
    | name=ID '(' (expr (',' expr)*)? ')'                      #ImplicitThisCallExpr    // implicit this.method(...)
    | expr '[' expr ']'                                        #ArrayLoadExpr
    | THIS                                                     #ThisExpr
    | NEW name=ID '(' (expr (',' expr)*)? ')'                  #NewExpr                 // object instantiation
    | NEW INT '[' ']' '{' (expr (',' expr)*)? '}'              #ArrayInitializer        // int[]{...}
    | NEW INT '[' expr ']' ('[' expr ']')* ('[' ']')*          #NewArrayExpr            // multi-dim array allocation
    // ── Unary operators ──
    | op='++' expr                                             #PlusPlusExpr
    | op='--' expr                                             #MinusMinusExpr
    | op='+' expr                                              #PlusExpr
    | op='-' expr                                              #MinusExpr
    | op='!' expr                                              #UnaryExpr
    // ── Binary operators (multiplicative → additive → relational → equality → logical) ──
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
    // ── Literals & variable references ──
    | value=INTEGER                                            #IntegerLiteral
    | value=BOOL                                               #BoolLiteral
    | name=ID                                                  #VarRefExpr
    ;
