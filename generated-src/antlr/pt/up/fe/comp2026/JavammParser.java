// Generated from pt/up/fe/comp2026/Javamm.g4 by ANTLR 4.5.3

    package pt.up.fe.comp2026;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JavammParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, CLASS=27, EXTENDS=28, INT=29, BOOLEAN=30, VOID=31, 
		STATIC=32, RETURN=33, PACKAGE=34, IMPORT=35, PUBLIC=36, PRIVATE=37, PROTECTED=38, 
		THIS=39, NEW=40, LENGTH=41, IF=42, ELSE=43, WHILE=44, FOR=45, DO=46, INTEGER=47, 
		BOOL=48, ID=49, WS=50, SINGLE_COMMENT=51, BLOCK_COMMENT=52;
	public static final int
		RULE_program = 0, RULE_stmtEntry = 1, RULE_expression = 2, RULE_importDecl = 3, 
		RULE_packageDecl = 4, RULE_classDecl = 5, RULE_classMember = 6, RULE_fieldDecl = 7, 
		RULE_varDecl = 8, RULE_param = 9, RULE_type = 10, RULE_methodDecl = 11, 
		RULE_assignment = 12, RULE_stmt = 13, RULE_forInit = 14, RULE_forCond = 15, 
		RULE_forUpdate = 16, RULE_expr = 17;
	public static final String[] ruleNames = {
		"program", "stmtEntry", "expression", "importDecl", "packageDecl", "classDecl", 
		"classMember", "fieldDecl", "varDecl", "param", "type", "methodDecl", 
		"assignment", "stmt", "forInit", "forCond", "forUpdate", "expr"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "';'", "'{'", "'}'", "'='", "'['", "']'", "'('", "','", "')'", 
		"'++'", "'--'", "'+'", "'-'", "'!'", "'*'", "'/'", "'%'", "'<'", "'>'", 
		"'<='", "'>='", "'=='", "'!='", "'&&'", "'||'", "'class'", "'extends'", 
		"'int'", "'boolean'", "'void'", "'static'", "'return'", "'package'", "'import'", 
		"'public'", "'private'", "'protected'", "'this'", "'new'", "'length'", 
		"'if'", "'else'", "'while'", "'for'", "'do'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, "CLASS", "EXTENDS", "INT", "BOOLEAN", "VOID", "STATIC", 
		"RETURN", "PACKAGE", "IMPORT", "PUBLIC", "PRIVATE", "PROTECTED", "THIS", 
		"NEW", "LENGTH", "IF", "ELSE", "WHILE", "FOR", "DO", "INTEGER", "BOOL", 
		"ID", "WS", "SINGLE_COMMENT", "BLOCK_COMMENT"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Javamm.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JavammParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ProgramContext extends ParserRuleContext {
		public ClassDeclContext classNode;
		public PackageDeclContext packageDecl() {
			return getRuleContext(PackageDeclContext.class,0);
		}
		public TerminalNode EOF() { return getToken(JavammParser.EOF, 0); }
		public ClassDeclContext classDecl() {
			return getRuleContext(ClassDeclContext.class,0);
		}
		public List<ImportDeclContext> importDecl() {
			return getRuleContexts(ImportDeclContext.class);
		}
		public ImportDeclContext importDecl(int i) {
			return getRuleContext(ImportDeclContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitProgram(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			packageDecl();
			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(37);
				importDecl();
				}
				}
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(43);
			((ProgramContext)_localctx).classNode = classDecl();
			setState(44);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StmtEntryContext extends ParserRuleContext {
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public TerminalNode EOF() { return getToken(JavammParser.EOF, 0); }
		public StmtEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmtEntry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterStmtEntry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitStmtEntry(this);
		}
	}

	public final StmtEntryContext stmtEntry() throws RecognitionException {
		StmtEntryContext _localctx = new StmtEntryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_stmtEntry);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			stmt();
			setState(47);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode EOF() { return getToken(JavammParser.EOF, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			expr(0);
			setState(50);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportDeclContext extends ParserRuleContext {
		public Token ID;
		public List<Token> path = new ArrayList<Token>();
		public TerminalNode IMPORT() { return getToken(JavammParser.IMPORT, 0); }
		public List<TerminalNode> ID() { return getTokens(JavammParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(JavammParser.ID, i);
		}
		public ImportDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterImportDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitImportDecl(this);
		}
	}

	public final ImportDeclContext importDecl() throws RecognitionException {
		ImportDeclContext _localctx = new ImportDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_importDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			match(IMPORT);
			setState(53);
			((ImportDeclContext)_localctx).ID = match(ID);
			((ImportDeclContext)_localctx).path.add(((ImportDeclContext)_localctx).ID);
			setState(54);
			match(T__0);
			setState(55);
			((ImportDeclContext)_localctx).ID = match(ID);
			((ImportDeclContext)_localctx).path.add(((ImportDeclContext)_localctx).ID);
			setState(60);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(56);
				match(T__0);
				setState(57);
				((ImportDeclContext)_localctx).ID = match(ID);
				((ImportDeclContext)_localctx).path.add(((ImportDeclContext)_localctx).ID);
				}
				}
				setState(62);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(63);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PackageDeclContext extends ParserRuleContext {
		public Token ID;
		public List<Token> path = new ArrayList<Token>();
		public TerminalNode PACKAGE() { return getToken(JavammParser.PACKAGE, 0); }
		public List<TerminalNode> ID() { return getTokens(JavammParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(JavammParser.ID, i);
		}
		public PackageDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterPackageDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitPackageDecl(this);
		}
	}

	public final PackageDeclContext packageDecl() throws RecognitionException {
		PackageDeclContext _localctx = new PackageDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_packageDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(PACKAGE);
			setState(66);
			((PackageDeclContext)_localctx).ID = match(ID);
			((PackageDeclContext)_localctx).path.add(((PackageDeclContext)_localctx).ID);
			setState(71);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(67);
				match(T__0);
				setState(68);
				((PackageDeclContext)_localctx).ID = match(ID);
				((PackageDeclContext)_localctx).path.add(((PackageDeclContext)_localctx).ID);
				}
				}
				setState(73);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(74);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassDeclContext extends ParserRuleContext {
		public Token name;
		public Token superName;
		public TerminalNode CLASS() { return getToken(JavammParser.CLASS, 0); }
		public List<TerminalNode> ID() { return getTokens(JavammParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(JavammParser.ID, i);
		}
		public TerminalNode EXTENDS() { return getToken(JavammParser.EXTENDS, 0); }
		public List<ClassMemberContext> classMember() {
			return getRuleContexts(ClassMemberContext.class);
		}
		public ClassMemberContext classMember(int i) {
			return getRuleContext(ClassMemberContext.class,i);
		}
		public ClassDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterClassDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitClassDecl(this);
		}
	}

	public final ClassDeclContext classDecl() throws RecognitionException {
		ClassDeclContext _localctx = new ClassDeclContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_classDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(CLASS);
			setState(77);
			((ClassDeclContext)_localctx).name = match(ID);
			setState(80);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(78);
				match(EXTENDS);
				setState(79);
				((ClassDeclContext)_localctx).superName = match(ID);
				}
			}

			setState(82);
			match(T__2);
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << BOOLEAN) | (1L << VOID) | (1L << STATIC) | (1L << PUBLIC) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << ID))) != 0)) {
				{
				{
				setState(83);
				classMember();
				}
				}
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(89);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassMemberContext extends ParserRuleContext {
		public FieldDeclContext fieldDecl() {
			return getRuleContext(FieldDeclContext.class,0);
		}
		public MethodDeclContext methodDecl() {
			return getRuleContext(MethodDeclContext.class,0);
		}
		public ClassMemberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classMember; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterClassMember(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitClassMember(this);
		}
	}

	public final ClassMemberContext classMember() throws RecognitionException {
		ClassMemberContext _localctx = new ClassMemberContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_classMember);
		try {
			setState(93);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(91);
				fieldDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				methodDecl();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldDeclContext extends ParserRuleContext {
		public TypeContext typeNode;
		public Token name;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public FieldDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterFieldDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitFieldDecl(this);
		}
	}

	public final FieldDeclContext fieldDecl() throws RecognitionException {
		FieldDeclContext _localctx = new FieldDeclContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fieldDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			((FieldDeclContext)_localctx).typeNode = type();
			setState(96);
			((FieldDeclContext)_localctx).name = match(ID);
			setState(99);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(97);
				match(T__4);
				setState(98);
				expr(0);
				}
			}

			setState(101);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VarDeclContext extends ParserRuleContext {
		public TypeContext typeNode;
		public Token name;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public VarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterVarDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitVarDecl(this);
		}
	}

	public final VarDeclContext varDecl() throws RecognitionException {
		VarDeclContext _localctx = new VarDeclContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_varDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			((VarDeclContext)_localctx).typeNode = type();
			setState(104);
			((VarDeclContext)_localctx).name = match(ID);
			setState(105);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParamContext extends ParserRuleContext {
		public TypeContext typeNode;
		public Token name;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitParam(this);
		}
	}

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			((ParamContext)_localctx).typeNode = type();
			setState(108);
			((ParamContext)_localctx).name = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeContext extends ParserRuleContext {
		public Token name;
		public Token s6;
		public List<Token> dims = new ArrayList<Token>();
		public Token s7;
		public TerminalNode INT() { return getToken(JavammParser.INT, 0); }
		public TerminalNode BOOLEAN() { return getToken(JavammParser.BOOLEAN, 0); }
		public TerminalNode VOID() { return getToken(JavammParser.VOID, 0); }
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitType(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			switch (_input.LA(1)) {
			case INT:
				{
				setState(110);
				((TypeContext)_localctx).name = match(INT);
				}
				break;
			case BOOLEAN:
				{
				setState(111);
				((TypeContext)_localctx).name = match(BOOLEAN);
				}
				break;
			case VOID:
				{
				setState(112);
				((TypeContext)_localctx).name = match(VOID);
				}
				break;
			case ID:
				{
				setState(113);
				((TypeContext)_localctx).name = match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(120);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(116);
				((TypeContext)_localctx).s6 = match(T__5);
				((TypeContext)_localctx).dims.add(((TypeContext)_localctx).s6);
				setState(117);
				((TypeContext)_localctx).s7 = match(T__6);
				((TypeContext)_localctx).dims.add(((TypeContext)_localctx).s7);
				}
				}
				setState(122);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MethodDeclContext extends ParserRuleContext {
		public boolean isStatic = false;
		public Token visibility;
		public TypeContext returnType;
		public Token name;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public TerminalNode STATIC() { return getToken(JavammParser.STATIC, 0); }
		public List<ParamContext> param() {
			return getRuleContexts(ParamContext.class);
		}
		public ParamContext param(int i) {
			return getRuleContext(ParamContext.class,i);
		}
		public List<VarDeclContext> varDecl() {
			return getRuleContexts(VarDeclContext.class);
		}
		public VarDeclContext varDecl(int i) {
			return getRuleContext(VarDeclContext.class,i);
		}
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public TerminalNode PUBLIC() { return getToken(JavammParser.PUBLIC, 0); }
		public TerminalNode PRIVATE() { return getToken(JavammParser.PRIVATE, 0); }
		public TerminalNode PROTECTED() { return getToken(JavammParser.PROTECTED, 0); }
		public MethodDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterMethodDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitMethodDecl(this);
		}
	}

	public final MethodDeclContext methodDecl() throws RecognitionException {
		MethodDeclContext _localctx = new MethodDeclContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_methodDecl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PUBLIC) | (1L << PRIVATE) | (1L << PROTECTED))) != 0)) {
				{
				setState(123);
				((MethodDeclContext)_localctx).visibility = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PUBLIC) | (1L << PRIVATE) | (1L << PROTECTED))) != 0)) ) {
					((MethodDeclContext)_localctx).visibility = (Token)_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
			}

			setState(128);
			_la = _input.LA(1);
			if (_la==STATIC) {
				{
				setState(126);
				match(STATIC);
				((MethodDeclContext)_localctx).isStatic = true;
				}
			}

			setState(130);
			((MethodDeclContext)_localctx).returnType = type();
			setState(131);
			((MethodDeclContext)_localctx).name = match(ID);
			setState(132);
			match(T__7);
			setState(141);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << BOOLEAN) | (1L << VOID) | (1L << ID))) != 0)) {
				{
				setState(133);
				param();
				setState(138);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__8) {
					{
					{
					setState(134);
					match(T__8);
					setState(135);
					param();
					}
					}
					setState(140);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(143);
			match(T__9);
			setState(144);
			match(T__2);
			setState(148);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(145);
					varDecl();
					}
					} 
				}
				setState(150);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			setState(154);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << RETURN) | (1L << THIS) | (1L << NEW) | (1L << IF) | (1L << WHILE) | (1L << FOR) | (1L << DO) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
				{
				{
				setState(151);
				stmt();
				}
				}
				setState(156);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(157);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignmentContext extends ParserRuleContext {
		public Token name;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitAssignment(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			((AssignmentContext)_localctx).name = match(ID);
			setState(160);
			match(T__4);
			setState(161);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StmtContext extends ParserRuleContext {
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
	 
		public StmtContext() { }
		public void copyFrom(StmtContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IfStmtContext extends StmtContext {
		public TerminalNode IF() { return getToken(JavammParser.IF, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public IfStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterIfStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitIfStmt(this);
		}
	}
	public static class ExprStmtContext extends StmtContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterExprStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitExprStmt(this);
		}
	}
	public static class WhileStmtContext extends StmtContext {
		public TerminalNode WHILE() { return getToken(JavammParser.WHILE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(JavammParser.ELSE, 0); }
		public WhileStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterWhileStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitWhileStmt(this);
		}
	}
	public static class IfElseStmtContext extends StmtContext {
		public TerminalNode IF() { return getToken(JavammParser.IF, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(JavammParser.ELSE, 0); }
		public IfElseStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterIfElseStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitIfElseStmt(this);
		}
	}
	public static class AssignStmtContext extends StmtContext {
		public Token var;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public AssignStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterAssignStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitAssignStmt(this);
		}
	}
	public static class BlockContext extends StmtContext {
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public BlockContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitBlock(this);
		}
	}
	public static class ForStmtContext extends StmtContext {
		public TerminalNode FOR() { return getToken(JavammParser.FOR, 0); }
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public ForInitContext forInit() {
			return getRuleContext(ForInitContext.class,0);
		}
		public ForCondContext forCond() {
			return getRuleContext(ForCondContext.class,0);
		}
		public ForUpdateContext forUpdate() {
			return getRuleContext(ForUpdateContext.class,0);
		}
		public ForStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterForStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitForStmt(this);
		}
	}
	public static class DoWhileStmtContext extends StmtContext {
		public TerminalNode DO() { return getToken(JavammParser.DO, 0); }
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public TerminalNode WHILE() { return getToken(JavammParser.WHILE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public DoWhileStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterDoWhileStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitDoWhileStmt(this);
		}
	}
	public static class ReturnStmtContext extends StmtContext {
		public TerminalNode RETURN() { return getToken(JavammParser.RETURN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ReturnStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterReturnStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitReturnStmt(this);
		}
	}
	public static class ArrayStoreStmtContext extends StmtContext {
		public Token name;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public ArrayStoreStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterArrayStoreStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitArrayStoreStmt(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_stmt);
		int _la;
		try {
			setState(247);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				_localctx = new BlockContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(163);
				match(T__2);
				setState(167);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << RETURN) | (1L << THIS) | (1L << NEW) | (1L << IF) | (1L << WHILE) | (1L << FOR) | (1L << DO) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
					{
					{
					setState(164);
					stmt();
					}
					}
					setState(169);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(170);
				match(T__3);
				}
				break;
			case 2:
				_localctx = new ForStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(171);
				match(FOR);
				setState(172);
				match(T__7);
				setState(174);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(173);
					forInit();
					}
				}

				setState(176);
				match(T__1);
				setState(178);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << THIS) | (1L << NEW) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
					{
					setState(177);
					forCond();
					}
				}

				setState(180);
				match(T__1);
				setState(182);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(181);
					forUpdate();
					}
				}

				setState(184);
				match(T__9);
				setState(185);
				stmt();
				}
				break;
			case 3:
				_localctx = new WhileStmtContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(186);
				match(WHILE);
				setState(187);
				match(T__7);
				setState(188);
				expr(0);
				setState(189);
				match(T__9);
				setState(190);
				stmt();
				setState(193);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
				case 1:
					{
					setState(191);
					match(ELSE);
					setState(192);
					stmt();
					}
					break;
				}
				}
				break;
			case 4:
				_localctx = new DoWhileStmtContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(195);
				match(DO);
				setState(196);
				stmt();
				setState(197);
				match(WHILE);
				setState(198);
				match(T__7);
				setState(199);
				expr(0);
				setState(200);
				match(T__9);
				setState(201);
				match(T__1);
				}
				break;
			case 5:
				_localctx = new IfElseStmtContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(203);
				match(IF);
				setState(204);
				match(T__7);
				setState(205);
				expr(0);
				setState(206);
				match(T__9);
				setState(207);
				stmt();
				setState(208);
				match(ELSE);
				setState(209);
				stmt();
				}
				break;
			case 6:
				_localctx = new IfStmtContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(211);
				match(IF);
				setState(212);
				match(T__7);
				setState(213);
				expr(0);
				setState(214);
				match(T__9);
				setState(215);
				stmt();
				}
				break;
			case 7:
				_localctx = new AssignStmtContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(217);
				((AssignStmtContext)_localctx).var = match(ID);
				setState(218);
				match(T__4);
				setState(219);
				expr(0);
				setState(220);
				match(T__1);
				}
				break;
			case 8:
				_localctx = new ArrayStoreStmtContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(222);
				((ArrayStoreStmtContext)_localctx).name = match(ID);
				setState(223);
				match(T__5);
				setState(224);
				expr(0);
				setState(225);
				match(T__6);
				setState(232);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(226);
					match(T__5);
					setState(227);
					expr(0);
					setState(228);
					match(T__6);
					}
					}
					setState(234);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(235);
				match(T__4);
				setState(236);
				expr(0);
				setState(237);
				match(T__1);
				}
				break;
			case 9:
				_localctx = new ReturnStmtContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(239);
				match(RETURN);
				setState(241);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << THIS) | (1L << NEW) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
					{
					setState(240);
					expr(0);
					}
				}

				setState(243);
				match(T__1);
				}
				break;
			case 10:
				_localctx = new ExprStmtContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(244);
				expr(0);
				setState(245);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForInitContext extends ParserRuleContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public ForInitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forInit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterForInit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitForInit(this);
		}
	}

	public final ForInitContext forInit() throws RecognitionException {
		ForInitContext _localctx = new ForInitContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_forInit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(249);
			assignment();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForCondContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ForCondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forCond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterForCond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitForCond(this);
		}
	}

	public final ForCondContext forCond() throws RecognitionException {
		ForCondContext _localctx = new ForCondContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_forCond);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForUpdateContext extends ParserRuleContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public ForUpdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forUpdate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterForUpdate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitForUpdate(this);
		}
	}

	public final ForUpdateContext forUpdate() throws RecognitionException {
		ForUpdateContext _localctx = new ForUpdateContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_forUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(253);
			assignment();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class PlusPlusExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public PlusPlusExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterPlusPlusExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitPlusPlusExpr(this);
		}
	}
	public static class LengthExprContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode LENGTH() { return getToken(JavammParser.LENGTH, 0); }
		public LengthExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterLengthExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitLengthExpr(this);
		}
	}
	public static class BinaryExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public BinaryExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterBinaryExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitBinaryExpr(this);
		}
	}
	public static class UnaryExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public UnaryExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterUnaryExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitUnaryExpr(this);
		}
	}
	public static class ArrayLoadExprContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ArrayLoadExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterArrayLoadExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitArrayLoadExpr(this);
		}
	}
	public static class MinusMinusExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public MinusMinusExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterMinusMinusExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitMinusMinusExpr(this);
		}
	}
	public static class PlusExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public PlusExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterPlusExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitPlusExpr(this);
		}
	}
	public static class BoolLiteralContext extends ExprContext {
		public Token value;
		public TerminalNode BOOL() { return getToken(JavammParser.BOOL, 0); }
		public BoolLiteralContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterBoolLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitBoolLiteral(this);
		}
	}
	public static class NewArrayExprContext extends ExprContext {
		public TerminalNode NEW() { return getToken(JavammParser.NEW, 0); }
		public TerminalNode INT() { return getToken(JavammParser.INT, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public NewArrayExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterNewArrayExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitNewArrayExpr(this);
		}
	}
	public static class VarRefExprContext extends ExprContext {
		public Token name;
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public VarRefExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterVarRefExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitVarRefExpr(this);
		}
	}
	public static class NewExprContext extends ExprContext {
		public Token name;
		public TerminalNode NEW() { return getToken(JavammParser.NEW, 0); }
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public NewExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterNewExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitNewExpr(this);
		}
	}
	public static class ImplicitThisCallExprContext extends ExprContext {
		public Token name;
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ImplicitThisCallExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterImplicitThisCallExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitImplicitThisCallExpr(this);
		}
	}
	public static class FieldAccessExprContext extends ExprContext {
		public Token name;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public FieldAccessExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterFieldAccessExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitFieldAccessExpr(this);
		}
	}
	public static class ParenExprContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ParenExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterParenExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitParenExpr(this);
		}
	}
	public static class ThisExprContext extends ExprContext {
		public TerminalNode THIS() { return getToken(JavammParser.THIS, 0); }
		public ThisExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterThisExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitThisExpr(this);
		}
	}
	public static class IntegerLiteralContext extends ExprContext {
		public Token value;
		public TerminalNode INTEGER() { return getToken(JavammParser.INTEGER, 0); }
		public IntegerLiteralContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitIntegerLiteral(this);
		}
	}
	public static class ArrayInitializerContext extends ExprContext {
		public TerminalNode NEW() { return getToken(JavammParser.NEW, 0); }
		public TerminalNode INT() { return getToken(JavammParser.INT, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ArrayInitializerContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitArrayInitializer(this);
		}
	}
	public static class MinusExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public MinusExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterMinusExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitMinusExpr(this);
		}
	}
	public static class MethodCallExprContext extends ExprContext {
		public Token name;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public MethodCallExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).enterMethodCallExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavammListener ) ((JavammListener)listener).exitMethodCallExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 34;
		enterRecursionRule(_localctx, 34, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				{
				_localctx = new ParenExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(256);
				match(T__7);
				setState(257);
				expr(0);
				setState(258);
				match(T__9);
				}
				break;
			case 2:
				{
				_localctx = new ImplicitThisCallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(260);
				((ImplicitThisCallExprContext)_localctx).name = match(ID);
				setState(261);
				match(T__7);
				setState(270);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << THIS) | (1L << NEW) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
					{
					setState(262);
					expr(0);
					setState(267);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__8) {
						{
						{
						setState(263);
						match(T__8);
						setState(264);
						expr(0);
						}
						}
						setState(269);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(272);
				match(T__9);
				}
				break;
			case 3:
				{
				_localctx = new ThisExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(273);
				match(THIS);
				}
				break;
			case 4:
				{
				_localctx = new NewExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(274);
				match(NEW);
				setState(275);
				((NewExprContext)_localctx).name = match(ID);
				setState(276);
				match(T__7);
				setState(285);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << THIS) | (1L << NEW) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
					{
					setState(277);
					expr(0);
					setState(282);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__8) {
						{
						{
						setState(278);
						match(T__8);
						setState(279);
						expr(0);
						}
						}
						setState(284);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(287);
				match(T__9);
				}
				break;
			case 5:
				{
				_localctx = new ArrayInitializerContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(288);
				match(NEW);
				setState(289);
				match(INT);
				setState(290);
				match(T__5);
				setState(291);
				match(T__6);
				setState(292);
				match(T__2);
				setState(301);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << THIS) | (1L << NEW) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
					{
					setState(293);
					expr(0);
					setState(298);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__8) {
						{
						{
						setState(294);
						match(T__8);
						setState(295);
						expr(0);
						}
						}
						setState(300);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(303);
				match(T__3);
				}
				break;
			case 6:
				{
				_localctx = new NewArrayExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(304);
				match(NEW);
				setState(305);
				match(INT);
				setState(306);
				match(T__5);
				setState(307);
				expr(0);
				setState(308);
				match(T__6);
				setState(315);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(309);
						match(T__5);
						setState(310);
						expr(0);
						setState(311);
						match(T__6);
						}
						} 
					}
					setState(317);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				}
				setState(322);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(318);
						match(T__5);
						setState(319);
						match(T__6);
						}
						} 
					}
					setState(324);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
				}
				}
				break;
			case 7:
				{
				_localctx = new PlusPlusExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(325);
				((PlusPlusExprContext)_localctx).op = match(T__10);
				setState(326);
				expr(21);
				}
				break;
			case 8:
				{
				_localctx = new MinusMinusExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(327);
				((MinusMinusExprContext)_localctx).op = match(T__11);
				setState(328);
				expr(20);
				}
				break;
			case 9:
				{
				_localctx = new PlusExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(329);
				((PlusExprContext)_localctx).op = match(T__12);
				setState(330);
				expr(19);
				}
				break;
			case 10:
				{
				_localctx = new MinusExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(331);
				((MinusExprContext)_localctx).op = match(T__13);
				setState(332);
				expr(18);
				}
				break;
			case 11:
				{
				_localctx = new UnaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(333);
				((UnaryExprContext)_localctx).op = match(T__14);
				setState(334);
				expr(17);
				}
				break;
			case 12:
				{
				_localctx = new IntegerLiteralContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(335);
				((IntegerLiteralContext)_localctx).value = match(INTEGER);
				}
				break;
			case 13:
				{
				_localctx = new BoolLiteralContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(336);
				((BoolLiteralContext)_localctx).value = match(BOOL);
				}
				break;
			case 14:
				{
				_localctx = new VarRefExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(337);
				((VarRefExprContext)_localctx).name = match(ID);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(407);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(405);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
					case 1:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(340);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(341);
						((BinaryExprContext)_localctx).op = match(T__15);
						setState(342);
						expr(17);
						}
						break;
					case 2:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(343);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(344);
						((BinaryExprContext)_localctx).op = match(T__16);
						setState(345);
						expr(16);
						}
						break;
					case 3:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(346);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(347);
						((BinaryExprContext)_localctx).op = match(T__17);
						setState(348);
						expr(15);
						}
						break;
					case 4:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(349);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(350);
						((BinaryExprContext)_localctx).op = match(T__12);
						setState(351);
						expr(14);
						}
						break;
					case 5:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(352);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(353);
						((BinaryExprContext)_localctx).op = match(T__13);
						setState(354);
						expr(13);
						}
						break;
					case 6:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(355);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(356);
						((BinaryExprContext)_localctx).op = match(T__18);
						setState(357);
						expr(12);
						}
						break;
					case 7:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(358);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(359);
						((BinaryExprContext)_localctx).op = match(T__19);
						setState(360);
						expr(11);
						}
						break;
					case 8:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(361);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(362);
						((BinaryExprContext)_localctx).op = match(T__20);
						setState(363);
						expr(10);
						}
						break;
					case 9:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(364);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(365);
						((BinaryExprContext)_localctx).op = match(T__21);
						setState(366);
						expr(9);
						}
						break;
					case 10:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(367);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(368);
						((BinaryExprContext)_localctx).op = match(T__22);
						setState(369);
						expr(8);
						}
						break;
					case 11:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(370);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(371);
						((BinaryExprContext)_localctx).op = match(T__23);
						setState(372);
						expr(7);
						}
						break;
					case 12:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(373);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(374);
						((BinaryExprContext)_localctx).op = match(T__24);
						setState(375);
						expr(6);
						}
						break;
					case 13:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(376);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(377);
						((BinaryExprContext)_localctx).op = match(T__25);
						setState(378);
						expr(5);
						}
						break;
					case 14:
						{
						_localctx = new LengthExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(379);
						if (!(precpred(_ctx, 30))) throw new FailedPredicateException(this, "precpred(_ctx, 30)");
						setState(380);
						match(T__0);
						setState(381);
						match(LENGTH);
						}
						break;
					case 15:
						{
						_localctx = new MethodCallExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(382);
						if (!(precpred(_ctx, 29))) throw new FailedPredicateException(this, "precpred(_ctx, 29)");
						setState(383);
						match(T__0);
						setState(384);
						((MethodCallExprContext)_localctx).name = match(ID);
						setState(385);
						match(T__7);
						setState(394);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__7) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << THIS) | (1L << NEW) | (1L << INTEGER) | (1L << BOOL) | (1L << ID))) != 0)) {
							{
							setState(386);
							expr(0);
							setState(391);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la==T__8) {
								{
								{
								setState(387);
								match(T__8);
								setState(388);
								expr(0);
								}
								}
								setState(393);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
							}
						}

						setState(396);
						match(T__9);
						}
						break;
					case 16:
						{
						_localctx = new FieldAccessExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(397);
						if (!(precpred(_ctx, 28))) throw new FailedPredicateException(this, "precpred(_ctx, 28)");
						setState(398);
						match(T__0);
						setState(399);
						((FieldAccessExprContext)_localctx).name = match(ID);
						}
						break;
					case 17:
						{
						_localctx = new ArrayLoadExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(400);
						if (!(precpred(_ctx, 26))) throw new FailedPredicateException(this, "precpred(_ctx, 26)");
						setState(401);
						match(T__5);
						setState(402);
						expr(0);
						setState(403);
						match(T__6);
						}
						break;
					}
					} 
				}
				setState(409);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 17:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 16);
		case 1:
			return precpred(_ctx, 15);
		case 2:
			return precpred(_ctx, 14);
		case 3:
			return precpred(_ctx, 13);
		case 4:
			return precpred(_ctx, 12);
		case 5:
			return precpred(_ctx, 11);
		case 6:
			return precpred(_ctx, 10);
		case 7:
			return precpred(_ctx, 9);
		case 8:
			return precpred(_ctx, 8);
		case 9:
			return precpred(_ctx, 7);
		case 10:
			return precpred(_ctx, 6);
		case 11:
			return precpred(_ctx, 5);
		case 12:
			return precpred(_ctx, 4);
		case 13:
			return precpred(_ctx, 30);
		case 14:
			return precpred(_ctx, 29);
		case 15:
			return precpred(_ctx, 28);
		case 16:
			return precpred(_ctx, 26);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\66\u019d\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\7\2)\n\2\f\2\16\2,\13\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4"+
		"\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\7\5=\n\5\f\5\16\5@\13\5\3\5\3\5\3\6\3"+
		"\6\3\6\3\6\7\6H\n\6\f\6\16\6K\13\6\3\6\3\6\3\7\3\7\3\7\3\7\5\7S\n\7\3"+
		"\7\3\7\7\7W\n\7\f\7\16\7Z\13\7\3\7\3\7\3\b\3\b\5\b`\n\b\3\t\3\t\3\t\3"+
		"\t\5\tf\n\t\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\5\f"+
		"u\n\f\3\f\3\f\7\fy\n\f\f\f\16\f|\13\f\3\r\5\r\177\n\r\3\r\3\r\5\r\u0083"+
		"\n\r\3\r\3\r\3\r\3\r\3\r\3\r\7\r\u008b\n\r\f\r\16\r\u008e\13\r\5\r\u0090"+
		"\n\r\3\r\3\r\3\r\7\r\u0095\n\r\f\r\16\r\u0098\13\r\3\r\7\r\u009b\n\r\f"+
		"\r\16\r\u009e\13\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\7\17\u00a8\n"+
		"\17\f\17\16\17\u00ab\13\17\3\17\3\17\3\17\3\17\5\17\u00b1\n\17\3\17\3"+
		"\17\5\17\u00b5\n\17\3\17\3\17\5\17\u00b9\n\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\5\17\u00c4\n\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\7\17\u00e9\n\17\f\17\16\17\u00ec\13\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\5\17\u00f4\n\17\3\17\3\17\3\17\3\17\5\17\u00fa\n\17\3\20\3\20\3\21\3"+
		"\21\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\7\23\u010c"+
		"\n\23\f\23\16\23\u010f\13\23\5\23\u0111\n\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\7\23\u011b\n\23\f\23\16\23\u011e\13\23\5\23\u0120\n\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\7\23\u012b\n\23\f\23\16"+
		"\23\u012e\13\23\5\23\u0130\n\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\7\23\u013c\n\23\f\23\16\23\u013f\13\23\3\23\3\23\7\23\u0143"+
		"\n\23\f\23\16\23\u0146\13\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3"+
		"\23\3\23\3\23\3\23\3\23\5\23\u0155\n\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\7\23\u0188\n\23\f\23\16\23\u018b\13\23\5\23\u018d\n\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\7\23\u0198\n\23\f\23\16\23\u019b\13"+
		"\23\3\23\2\3$\24\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$\2\3\3\2&("+
		"\u01d3\2&\3\2\2\2\4\60\3\2\2\2\6\63\3\2\2\2\b\66\3\2\2\2\nC\3\2\2\2\f"+
		"N\3\2\2\2\16_\3\2\2\2\20a\3\2\2\2\22i\3\2\2\2\24m\3\2\2\2\26t\3\2\2\2"+
		"\30~\3\2\2\2\32\u00a1\3\2\2\2\34\u00f9\3\2\2\2\36\u00fb\3\2\2\2 \u00fd"+
		"\3\2\2\2\"\u00ff\3\2\2\2$\u0154\3\2\2\2&*\5\n\6\2\')\5\b\5\2(\'\3\2\2"+
		"\2),\3\2\2\2*(\3\2\2\2*+\3\2\2\2+-\3\2\2\2,*\3\2\2\2-.\5\f\7\2./\7\2\2"+
		"\3/\3\3\2\2\2\60\61\5\34\17\2\61\62\7\2\2\3\62\5\3\2\2\2\63\64\5$\23\2"+
		"\64\65\7\2\2\3\65\7\3\2\2\2\66\67\7%\2\2\678\7\63\2\289\7\3\2\29>\7\63"+
		"\2\2:;\7\3\2\2;=\7\63\2\2<:\3\2\2\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?A\3"+
		"\2\2\2@>\3\2\2\2AB\7\4\2\2B\t\3\2\2\2CD\7$\2\2DI\7\63\2\2EF\7\3\2\2FH"+
		"\7\63\2\2GE\3\2\2\2HK\3\2\2\2IG\3\2\2\2IJ\3\2\2\2JL\3\2\2\2KI\3\2\2\2"+
		"LM\7\4\2\2M\13\3\2\2\2NO\7\35\2\2OR\7\63\2\2PQ\7\36\2\2QS\7\63\2\2RP\3"+
		"\2\2\2RS\3\2\2\2ST\3\2\2\2TX\7\5\2\2UW\5\16\b\2VU\3\2\2\2WZ\3\2\2\2XV"+
		"\3\2\2\2XY\3\2\2\2Y[\3\2\2\2ZX\3\2\2\2[\\\7\6\2\2\\\r\3\2\2\2]`\5\20\t"+
		"\2^`\5\30\r\2_]\3\2\2\2_^\3\2\2\2`\17\3\2\2\2ab\5\26\f\2be\7\63\2\2cd"+
		"\7\7\2\2df\5$\23\2ec\3\2\2\2ef\3\2\2\2fg\3\2\2\2gh\7\4\2\2h\21\3\2\2\2"+
		"ij\5\26\f\2jk\7\63\2\2kl\7\4\2\2l\23\3\2\2\2mn\5\26\f\2no\7\63\2\2o\25"+
		"\3\2\2\2pu\7\37\2\2qu\7 \2\2ru\7!\2\2su\7\63\2\2tp\3\2\2\2tq\3\2\2\2t"+
		"r\3\2\2\2ts\3\2\2\2uz\3\2\2\2vw\7\b\2\2wy\7\t\2\2xv\3\2\2\2y|\3\2\2\2"+
		"zx\3\2\2\2z{\3\2\2\2{\27\3\2\2\2|z\3\2\2\2}\177\t\2\2\2~}\3\2\2\2~\177"+
		"\3\2\2\2\177\u0082\3\2\2\2\u0080\u0081\7\"\2\2\u0081\u0083\b\r\1\2\u0082"+
		"\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0085\5\26"+
		"\f\2\u0085\u0086\7\63\2\2\u0086\u008f\7\n\2\2\u0087\u008c\5\24\13\2\u0088"+
		"\u0089\7\13\2\2\u0089\u008b\5\24\13\2\u008a\u0088\3\2\2\2\u008b\u008e"+
		"\3\2\2\2\u008c\u008a\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u0090\3\2\2\2\u008e"+
		"\u008c\3\2\2\2\u008f\u0087\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0091\3\2"+
		"\2\2\u0091\u0092\7\f\2\2\u0092\u0096\7\5\2\2\u0093\u0095\5\22\n\2\u0094"+
		"\u0093\3\2\2\2\u0095\u0098\3\2\2\2\u0096\u0094\3\2\2\2\u0096\u0097\3\2"+
		"\2\2\u0097\u009c\3\2\2\2\u0098\u0096\3\2\2\2\u0099\u009b\5\34\17\2\u009a"+
		"\u0099\3\2\2\2\u009b\u009e\3\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d\3\2"+
		"\2\2\u009d\u009f\3\2\2\2\u009e\u009c\3\2\2\2\u009f\u00a0\7\6\2\2\u00a0"+
		"\31\3\2\2\2\u00a1\u00a2\7\63\2\2\u00a2\u00a3\7\7\2\2\u00a3\u00a4\5$\23"+
		"\2\u00a4\33\3\2\2\2\u00a5\u00a9\7\5\2\2\u00a6\u00a8\5\34\17\2\u00a7\u00a6"+
		"\3\2\2\2\u00a8\u00ab\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa"+
		"\u00ac\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ac\u00fa\7\6\2\2\u00ad\u00ae\7/"+
		"\2\2\u00ae\u00b0\7\n\2\2\u00af\u00b1\5\36\20\2\u00b0\u00af\3\2\2\2\u00b0"+
		"\u00b1\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b4\7\4\2\2\u00b3\u00b5\5 "+
		"\21\2\u00b4\u00b3\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6"+
		"\u00b8\7\4\2\2\u00b7\u00b9\5\"\22\2\u00b8\u00b7\3\2\2\2\u00b8\u00b9\3"+
		"\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00bb\7\f\2\2\u00bb\u00fa\5\34\17\2\u00bc"+
		"\u00bd\7.\2\2\u00bd\u00be\7\n\2\2\u00be\u00bf\5$\23\2\u00bf\u00c0\7\f"+
		"\2\2\u00c0\u00c3\5\34\17\2\u00c1\u00c2\7-\2\2\u00c2\u00c4\5\34\17\2\u00c3"+
		"\u00c1\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4\u00fa\3\2\2\2\u00c5\u00c6\7\60"+
		"\2\2\u00c6\u00c7\5\34\17\2\u00c7\u00c8\7.\2\2\u00c8\u00c9\7\n\2\2\u00c9"+
		"\u00ca\5$\23\2\u00ca\u00cb\7\f\2\2\u00cb\u00cc\7\4\2\2\u00cc\u00fa\3\2"+
		"\2\2\u00cd\u00ce\7,\2\2\u00ce\u00cf\7\n\2\2\u00cf\u00d0\5$\23\2\u00d0"+
		"\u00d1\7\f\2\2\u00d1\u00d2\5\34\17\2\u00d2\u00d3\7-\2\2\u00d3\u00d4\5"+
		"\34\17\2\u00d4\u00fa\3\2\2\2\u00d5\u00d6\7,\2\2\u00d6\u00d7\7\n\2\2\u00d7"+
		"\u00d8\5$\23\2\u00d8\u00d9\7\f\2\2\u00d9\u00da\5\34\17\2\u00da\u00fa\3"+
		"\2\2\2\u00db\u00dc\7\63\2\2\u00dc\u00dd\7\7\2\2\u00dd\u00de\5$\23\2\u00de"+
		"\u00df\7\4\2\2\u00df\u00fa\3\2\2\2\u00e0\u00e1\7\63\2\2\u00e1\u00e2\7"+
		"\b\2\2\u00e2\u00e3\5$\23\2\u00e3\u00ea\7\t\2\2\u00e4\u00e5\7\b\2\2\u00e5"+
		"\u00e6\5$\23\2\u00e6\u00e7\7\t\2\2\u00e7\u00e9\3\2\2\2\u00e8\u00e4\3\2"+
		"\2\2\u00e9\u00ec\3\2\2\2\u00ea\u00e8\3\2\2\2\u00ea\u00eb\3\2\2\2\u00eb"+
		"\u00ed\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ed\u00ee\7\7\2\2\u00ee\u00ef\5$"+
		"\23\2\u00ef\u00f0\7\4\2\2\u00f0\u00fa\3\2\2\2\u00f1\u00f3\7#\2\2\u00f2"+
		"\u00f4\5$\23\2\u00f3\u00f2\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4\u00f5\3\2"+
		"\2\2\u00f5\u00fa\7\4\2\2\u00f6\u00f7\5$\23\2\u00f7\u00f8\7\4\2\2\u00f8"+
		"\u00fa\3\2\2\2\u00f9\u00a5\3\2\2\2\u00f9\u00ad\3\2\2\2\u00f9\u00bc\3\2"+
		"\2\2\u00f9\u00c5\3\2\2\2\u00f9\u00cd\3\2\2\2\u00f9\u00d5\3\2\2\2\u00f9"+
		"\u00db\3\2\2\2\u00f9\u00e0\3\2\2\2\u00f9\u00f1\3\2\2\2\u00f9\u00f6\3\2"+
		"\2\2\u00fa\35\3\2\2\2\u00fb\u00fc\5\32\16\2\u00fc\37\3\2\2\2\u00fd\u00fe"+
		"\5$\23\2\u00fe!\3\2\2\2\u00ff\u0100\5\32\16\2\u0100#\3\2\2\2\u0101\u0102"+
		"\b\23\1\2\u0102\u0103\7\n\2\2\u0103\u0104\5$\23\2\u0104\u0105\7\f\2\2"+
		"\u0105\u0155\3\2\2\2\u0106\u0107\7\63\2\2\u0107\u0110\7\n\2\2\u0108\u010d"+
		"\5$\23\2\u0109\u010a\7\13\2\2\u010a\u010c\5$\23\2\u010b\u0109\3\2\2\2"+
		"\u010c\u010f\3\2\2\2\u010d\u010b\3\2\2\2\u010d\u010e\3\2\2\2\u010e\u0111"+
		"\3\2\2\2\u010f\u010d\3\2\2\2\u0110\u0108\3\2\2\2\u0110\u0111\3\2\2\2\u0111"+
		"\u0112\3\2\2\2\u0112\u0155\7\f\2\2\u0113\u0155\7)\2\2\u0114\u0115\7*\2"+
		"\2\u0115\u0116\7\63\2\2\u0116\u011f\7\n\2\2\u0117\u011c\5$\23\2\u0118"+
		"\u0119\7\13\2\2\u0119\u011b\5$\23\2\u011a\u0118\3\2\2\2\u011b\u011e\3"+
		"\2\2\2\u011c\u011a\3\2\2\2\u011c\u011d\3\2\2\2\u011d\u0120\3\2\2\2\u011e"+
		"\u011c\3\2\2\2\u011f\u0117\3\2\2\2\u011f\u0120\3\2\2\2\u0120\u0121\3\2"+
		"\2\2\u0121\u0155\7\f\2\2\u0122\u0123\7*\2\2\u0123\u0124\7\37\2\2\u0124"+
		"\u0125\7\b\2\2\u0125\u0126\7\t\2\2\u0126\u012f\7\5\2\2\u0127\u012c\5$"+
		"\23\2\u0128\u0129\7\13\2\2\u0129\u012b\5$\23\2\u012a\u0128\3\2\2\2\u012b"+
		"\u012e\3\2\2\2\u012c\u012a\3\2\2\2\u012c\u012d\3\2\2\2\u012d\u0130\3\2"+
		"\2\2\u012e\u012c\3\2\2\2\u012f\u0127\3\2\2\2\u012f\u0130\3\2\2\2\u0130"+
		"\u0131\3\2\2\2\u0131\u0155\7\6\2\2\u0132\u0133\7*\2\2\u0133\u0134\7\37"+
		"\2\2\u0134\u0135\7\b\2\2\u0135\u0136\5$\23\2\u0136\u013d\7\t\2\2\u0137"+
		"\u0138\7\b\2\2\u0138\u0139\5$\23\2\u0139\u013a\7\t\2\2\u013a\u013c\3\2"+
		"\2\2\u013b\u0137\3\2\2\2\u013c\u013f\3\2\2\2\u013d\u013b\3\2\2\2\u013d"+
		"\u013e\3\2\2\2\u013e\u0144\3\2\2\2\u013f\u013d\3\2\2\2\u0140\u0141\7\b"+
		"\2\2\u0141\u0143\7\t\2\2\u0142\u0140\3\2\2\2\u0143\u0146\3\2\2\2\u0144"+
		"\u0142\3\2\2\2\u0144\u0145\3\2\2\2\u0145\u0155\3\2\2\2\u0146\u0144\3\2"+
		"\2\2\u0147\u0148\7\r\2\2\u0148\u0155\5$\23\27\u0149\u014a\7\16\2\2\u014a"+
		"\u0155\5$\23\26\u014b\u014c\7\17\2\2\u014c\u0155\5$\23\25\u014d\u014e"+
		"\7\20\2\2\u014e\u0155\5$\23\24\u014f\u0150\7\21\2\2\u0150\u0155\5$\23"+
		"\23\u0151\u0155\7\61\2\2\u0152\u0155\7\62\2\2\u0153\u0155\7\63\2\2\u0154"+
		"\u0101\3\2\2\2\u0154\u0106\3\2\2\2\u0154\u0113\3\2\2\2\u0154\u0114\3\2"+
		"\2\2\u0154\u0122\3\2\2\2\u0154\u0132\3\2\2\2\u0154\u0147\3\2\2\2\u0154"+
		"\u0149\3\2\2\2\u0154\u014b\3\2\2\2\u0154\u014d\3\2\2\2\u0154\u014f\3\2"+
		"\2\2\u0154\u0151\3\2\2\2\u0154\u0152\3\2\2\2\u0154\u0153\3\2\2\2\u0155"+
		"\u0199\3\2\2\2\u0156\u0157\f\22\2\2\u0157\u0158\7\22\2\2\u0158\u0198\5"+
		"$\23\23\u0159\u015a\f\21\2\2\u015a\u015b\7\23\2\2\u015b\u0198\5$\23\22"+
		"\u015c\u015d\f\20\2\2\u015d\u015e\7\24\2\2\u015e\u0198\5$\23\21\u015f"+
		"\u0160\f\17\2\2\u0160\u0161\7\17\2\2\u0161\u0198\5$\23\20\u0162\u0163"+
		"\f\16\2\2\u0163\u0164\7\20\2\2\u0164\u0198\5$\23\17\u0165\u0166\f\r\2"+
		"\2\u0166\u0167\7\25\2\2\u0167\u0198\5$\23\16\u0168\u0169\f\f\2\2\u0169"+
		"\u016a\7\26\2\2\u016a\u0198\5$\23\r\u016b\u016c\f\13\2\2\u016c\u016d\7"+
		"\27\2\2\u016d\u0198\5$\23\f\u016e\u016f\f\n\2\2\u016f\u0170\7\30\2\2\u0170"+
		"\u0198\5$\23\13\u0171\u0172\f\t\2\2\u0172\u0173\7\31\2\2\u0173\u0198\5"+
		"$\23\n\u0174\u0175\f\b\2\2\u0175\u0176\7\32\2\2\u0176\u0198\5$\23\t\u0177"+
		"\u0178\f\7\2\2\u0178\u0179\7\33\2\2\u0179\u0198\5$\23\b\u017a\u017b\f"+
		"\6\2\2\u017b\u017c\7\34\2\2\u017c\u0198\5$\23\7\u017d\u017e\f \2\2\u017e"+
		"\u017f\7\3\2\2\u017f\u0198\7+\2\2\u0180\u0181\f\37\2\2\u0181\u0182\7\3"+
		"\2\2\u0182\u0183\7\63\2\2\u0183\u018c\7\n\2\2\u0184\u0189\5$\23\2\u0185"+
		"\u0186\7\13\2\2\u0186\u0188\5$\23\2\u0187\u0185\3\2\2\2\u0188\u018b\3"+
		"\2\2\2\u0189\u0187\3\2\2\2\u0189\u018a\3\2\2\2\u018a\u018d\3\2\2\2\u018b"+
		"\u0189\3\2\2\2\u018c\u0184\3\2\2\2\u018c\u018d\3\2\2\2\u018d\u018e\3\2"+
		"\2\2\u018e\u0198\7\f\2\2\u018f\u0190\f\36\2\2\u0190\u0191\7\3\2\2\u0191"+
		"\u0198\7\63\2\2\u0192\u0193\f\34\2\2\u0193\u0194\7\b\2\2\u0194\u0195\5"+
		"$\23\2\u0195\u0196\7\t\2\2\u0196\u0198\3\2\2\2\u0197\u0156\3\2\2\2\u0197"+
		"\u0159\3\2\2\2\u0197\u015c\3\2\2\2\u0197\u015f\3\2\2\2\u0197\u0162\3\2"+
		"\2\2\u0197\u0165\3\2\2\2\u0197\u0168\3\2\2\2\u0197\u016b\3\2\2\2\u0197"+
		"\u016e\3\2\2\2\u0197\u0171\3\2\2\2\u0197\u0174\3\2\2\2\u0197\u0177\3\2"+
		"\2\2\u0197\u017a\3\2\2\2\u0197\u017d\3\2\2\2\u0197\u0180\3\2\2\2\u0197"+
		"\u018f\3\2\2\2\u0197\u0192\3\2\2\2\u0198\u019b\3\2\2\2\u0199\u0197\3\2"+
		"\2\2\u0199\u019a\3\2\2\2\u019a%\3\2\2\2\u019b\u0199\3\2\2\2&*>IRX_etz"+
		"~\u0082\u008c\u008f\u0096\u009c\u00a9\u00b0\u00b4\u00b8\u00c3\u00ea\u00f3"+
		"\u00f9\u010d\u0110\u011c\u011f\u012c\u012f\u013d\u0144\u0154\u0189\u018c"+
		"\u0197\u0199";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}