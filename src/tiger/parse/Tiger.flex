/* Java preamble: */
package tiger.parse;
import tiger.errormsg.ErrorMsg;

%% 

%function next_token
%type java_cup.runtime.Symbol

%{
StringBuffer string = new StringBuffer();
int count;

private void newline() {
  errorMsg.newline(yychar);
}

private void err(int pos, String s) {
	errorMsg.error(pos, s);
}

private void err(String s) {
	err(yychar, s);
}

private java_cup.runtime.Symbol tok(int kind, Object value) {    
	return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

public Yylex(java.io.InputStream s, ErrorMsg e) {
  this(s);
  errorMsg = e;
}

private ErrorMsg errorMsg;

%}

/* JLex definitions: */
%cup
%char
%state STRING
%state STRING1
%state COMMENT

%eofval{
{ 
	if (yystate()==COMMENT) err("Comment symbol don't match!");
	if (yystate()==STRING) err("String presentation error!");
	if (yystate()==STRING1) err("String presentation error!");
	return tok(sym.EOF, null);
}
%eofval}       

LineTerminator = \r|\n|\r\n|\n\r
Identifier = [a-zA-Z][:jletterdigit:]*
DecIntegerLiteral = [0-9]+
WhiteSpace = [ \t\f]


%%
/* Regular expressions and actions: */
<YYINITIAL> {
	{LineTerminator}	{ newline(); }
	{WhiteSpace}	{ /* do nothing */ }
	
	/* Token : Keywords */
	"array" { return tok(sym.ARRAY, null); }
	"break" { return tok(sym.BREAK, null); }
	"do" { return tok(sym.DO, null); }
	"else" { return tok(sym.ELSE, null); }
	"end" { return tok(sym.END, null); }
	"for"  { return tok(sym.FOR, null); }
	"function" { return tok(sym.FUNCTION, null); }
	"if"  { return tok(sym.IF, null); }
	"in" { return tok(sym.IN, null); }
	"let" { return tok(sym.LET, null); }
	"nil" { return tok(sym.NIL, null); }
	"of" { return tok(sym.OF, null); }
	"then" { return tok(sym.THEN, null); }
	"to" { return tok(sym.TO, null); }
	"type" { return tok(sym.TYPE, null); }
	"var" { return tok(sym.VAR, null); }
	"while" { return tok(sym.WHILE, null); }
	
	/* Token : Identifiers */
	{Identifier} { return tok(sym.ID, yytext()); }
	
	/* Token : Integer */
	{DecIntegerLiteral} { return tok(sym.INT, new Integer(yytext())); }
	// should we check very long integer in there?
	/*[0-9]+ { return tok(sym.INT, new Integer(yytext())); }*/
	// or not?
	/*[0-9]+ { return tok(sym.INT, new String(yytext())); }*/
	
	/* Token : String */
  	\" { string.setLength(0);yybegin(STRING); }
  	
  	/* Token : SEPARATORS AND OPERATORS */
	","	{ return tok(sym.COMMA, null); }
	":" { return tok(sym.COLON, null); }
	";" { return tok(sym.SEMICOLON, null); }
	"(" { return tok(sym.LPAREN, null); }
	")" { return tok(sym.RPAREN, null); }
	"[" { return tok(sym.LBRACK, null); }
	"]" { return tok(sym.RBRACK, null); }
	"{" { return tok(sym.LBRACE, null); }
	"}" { return tok(sym.RBRACE, null); }
	"."	{ return tok(sym.DOT, null); }
	"+"	{ return tok(sym.PLUS, null); }
	"-" { return tok(sym.MINUS, null); }
	"*" { return tok(sym.TIMES, null); } 
	"/" { return tok(sym.DIVIDE, null); }
	"=" { return tok(sym.EQ, null); }
	"<>" { return tok(sym.NEQ, null); }
	"<" { return tok(sym.LT, null); }
	"<=" { return tok(sym.LE, null); } 
	">" { return tok(sym.GT, null); }
	">=" { return tok(sym.GE, null); }
	"&" { return tok(sym.AND, null); }
	"|" { return tok(sym.OR, null); }
	":=" { return tok(sym.ASSIGN, null); }
	
	"/*" { count=1; yybegin(COMMENT); }
	"*/" { err("Comment symbol don't match!"); }
	[^] { return tok(sym.error, yytext()); /* err("Illegal character < "+yytext()+" >!");*/ }
}

<STRING> {
	\" { yybegin(YYINITIAL); return tok(sym.STRING,string.toString()); }
	\\[0-9][0-9][0-9] { int tmp=Integer.parseInt(yytext().substring(1, 4)); if(tmp>255) err("exceed \\ddd"); else string.append((char)tmp); }
	[^\n\t\"\\]+ { string.append(yytext()); }
	\\t { string.append('\t'); }
	\\n { string.append('\n'); }
	\\\" { string.append('\"'); }
	\\\\ { string.append('\\'); }
	{LineTerminator} { err("String presentation error!"); }
	\\ { yybegin(STRING1); }
}

<STRING1> {
	{WhiteSpace} {} 
	" " {}
	\\ { yybegin(STRING); }
	\" { err("\\dont match"); }
	[^] { string.append(yytext()); }
}


<COMMENT> {
	"/*" { count++; }
	"*/" { count--; if (count==0) { yybegin(YYINITIAL); } }
	[^] {}
}