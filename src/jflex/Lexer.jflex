package java_cup;

import java_cup.runtime.Scanner;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;
import java.io.InputStreamReader;

%%

%unicode
%line
%column

%public
%class Lexer
%implements Scanner
%function next_token
%type Symbol

%{
    public Lexer(ComplexSymbolFactory csf) {
        this(new InputStreamReader(System.in));
        symbolFactory = csf;
    }

    private StringBuilder sb = new StringBuilder();
    private ComplexSymbolFactory symbolFactory;
    private int csline, cscolumn;

    public Symbol symbol(String name, int code) {
        Location xleft = new Location(yyline + 1, yycolumn + 1 - yylength());
        Location xright = new Location(yyline + 1, yycolumn + 1);
        return symbolFactory.newSymbol(name, code, xleft, xright);
    }

    public Symbol symbol(String name, int code, String lexeme) {
        Location xleft = new Location(yyline + 1, yycolumn + 1);
        Location xright = new Location(yyline + 1, yycolumn + yylength());
        return symbolFactory.newSymbol(name, code, xleft, xright, lexeme);
    }

    public void enterCodeSeg() {
        sb.setLength(0);
        csline = yyline + 1;
        cscolumn = yycolumn + 1;
    }

    public Symbol leaveCodeSeg() {
        Location xleft = new Location(csline, cscolumn);
        Location xright = new Location(yyline + 1, yycolumn + 1 + yylength());
        return symbolFactory.newSymbol("CODE_STRING", Symbols.CODE_STRING, xleft, xright, sb.toString());
    }

    protected String tellPos() {
        return "(" + (yyline + 1) + ':' + (yycolumn + 1) + ')';
    }

    protected void emit_warning(String message) {
        ErrorManager.getManager().emit_warning("Scanner at " + tellPos() + ": " + message);
    }

    protected void emit_error(String message) {
        ErrorManager.getManager().emit_error("Scanner at " + tellPos() +  ": " + message);
    }
%}

Newline = \r | \n | \r\n
Whitespace = [ \t\f] | {Newline}

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment = "/*" {CommentContent} \*+ "/"
EndOfLineComment = "//" [^\r\n]* {Newline}
CommentContent = ( [^*] | \*+[^*/] )*

ident = ([:jletter:] | "_" ) ([:jletterdigit:] | [:jletter:] | "_" )*


%eofval{
    return symbol("EOF", Symbols.EOF);
%eofval}

%state CODESEG

%%

<YYINITIAL> {

  {Whitespace}  {                                                                         }
  "?"           { return symbol("QUESTION", Symbols.QUESTION);                            }
  ";"           { return symbol("SEMI", Symbols.SEMI);                                    }
  ","           { return symbol("COMMA", Symbols.COMMA);                                  }
  "*"           { return symbol("STAR", Symbols.STAR);                                    }
  "."           { return symbol("DOT", Symbols.DOT);                                      }
  "|"           { return symbol("BAR", Symbols.BAR);                                      }
  "["           { return symbol("LBRACK", Symbols.LBRACK);                                }
  "]"           { return symbol("RBRACK", Symbols.RBRACK);                                }
  ":"           { return symbol("COLON", Symbols.COLON);                                  }
  "::="         { return symbol("COLON_COLON_EQUALS", Symbols.COLON_COLON_EQUALS);        }
  "%prec"       { return symbol("PERCENT_PREC", Symbols.PERCENT_PREC);                    }
  ">"           { return symbol("GT", Symbols.GT);                                        }
  "<"           { return symbol("LT", Symbols.LT);                                        }
  {Comment}     {                                                                         }
  "{:"          { enterCodeSeg(); yybegin(CODESEG);                                       }
  "package"     { return symbol("PACKAGE", Symbols.PACKAGE);                              }
  "import"      { return symbol("IMPORT", Symbols.IMPORT);                                }
  "static"      { return symbol("STATIC", Symbols.STATIC);                                }
  "class"       { return symbol("CLASS", Symbols.CLASS);                                  }
  "code"        { return symbol("CODE", Symbols.CODE);                                    }
  "action"      { return symbol("ACTION", Symbols.ACTION);                                }
  "parser"      { return symbol("PARSER", Symbols.PARSER);                                }
  "terminal"    { return symbol("TERMINAL", Symbols.TERMINAL);                            }
  "non"         { return symbol("NON", Symbols.NON);                                      }
  "nonterminal" { return symbol("NONTERMINAL", Symbols.NONTERMINAL);                      }
  "init"        { return symbol("INIT", Symbols.INIT);                                    }
  "scan"        { return symbol("SCAN", Symbols.SCAN);                                    }
  "with"        { return symbol("WITH", Symbols.WITH);                                    }
  "start"       { return symbol("START", Symbols.START);                                  }
  "precedence"  { return symbol("PRECEDENCE", Symbols.PRECEDENCE);                        }
  "left"        { return symbol("LEFT", Symbols.LEFT);                                    }
  "right"       { return symbol("RIGHT", Symbols.RIGHT);                                  }
  "nonassoc"    { return symbol("NONASSOC", Symbols.NONASSOC);                            }
  "extends"     { return symbol("EXTENDS", Symbols.EXTENDS);                              }
  "super"       { return symbol("SUPER", Symbols.SUPER);                                  }
  {ident}       { return symbol("ID", Symbols.ID, yytext());                              }

}

<CODESEG> {
  ":}"          { yybegin(YYINITIAL); return leaveCodeSeg();                              }
  .|\n          { sb.append(yytext());                                                    }
}

// error fallback
.|\n            { emit_warning("Unrecognized character '" + yytext() + "' -- ignored");   }
