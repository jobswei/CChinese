package front;

import java.util.regex.Pattern;

public class Token {
    public enum Type {
        MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK,
        IFTK, ELSETK,
        WHILETK,
        GETINTTK, PRINTFTK,
        RETURNTK, VOIDTK,
        SINGLECOMMENT, MULTICOMMENT,
        MATPLUS,MATMINU,MATMULT,MATDOTMULT,PLUS, MINU,
        MULT, DIV, MOD,
        LEQ, GEQ, EQL, NEQ,
        AND, OR,
        NOT, LSS, GRE, ASSIGN,
        SEMICN, COMMA,
        LPARENT, RPARENT,
        LBRACK, RBRACK,
        LBRACE, RBRACE,
        IDENFR, INTCON, STRCON,
        WHITESPACE
    }

    public static final String MAINTK_P = "(?<MAINTK>(main|主函数)(?![a-zA-Z0-9_]))";
    public static final String CONSTTK_P = "(?<CONSTTK>(const|常量)(?![a-zA-Z0-9_]))";
    public static final String INTTK_P = "(?<INTTK>(int|整数)(?![a-zA-Z0-9_]))";
    public static final String BREAKTK_P = "(?<BREAKTK>(break|跳出)(?![a-zA-Z0-9_]))";
    public static final String CONTINUETK_P = "(?<CONTINUETK>(continue|继续)(?![a-zA-Z0-9_]))";
    public static final String IFTK_P = "(?<IFTK>(if|如果)(?![a-zA-Z0-9_]))";
    public static final String ELSETK_P = "(?<ELSETK>(else|否则)(?![a-zA-Z0-9_]))";
    public static final String WHILETK_P = "(?<WHILETK>(while|当)(?![a-zA-Z0-9_]))";
    public static final String GETINTTK_P = "(?<GETINTTK>(getint|取整)(?![a-zA-Z0-9_]))";
    public static final String PRINTFTK_P = "(?<PRINTFTK>(printf|打印)(?![a-zA-Z0-9_]))";
    public static final String RETURNTK_P = "(?<RETURNTK>(return|返回)(?![a-zA-Z0-9_]))";
    public static final String VOIDTK_P = "(?<VOIDTK>(void|空)(?![a-zA-Z0-9_]))";
    public static final String SINGLECOMMENT_P = "(?<SINGLECOMMENT>//.*)";
    public static final String MULTICOMMENT_P = "(?<MULTICOMMENT>/\\*[\\s\\S]*?\\*/)";
    public static final String MATPLUS_P = "(?<MATPLUS>$)";
    public static final String MATMINU_P = "(?<MATMINU>\\^)";
    public static final String MATMULT_P = "(?<MATMULT>#)";
    public static final String MATDOTMULT_P = "(?<MATDOTMULT>@)";
    public static final String PLUS_P = "(?<PLUS>(\\+|加))";
    public static final String MINU_P = "(?<MINU>(-|减))";
    public static final String MULT_P = "(?<MULT>(\\*|乘以|乘))";
    public static final String DIV_P = "(?<DIV>(/|除以))";
    public static final String MOD_P = "(?<MOD>(%|模))";
    public static final String LEQ_P = "(?<LEQ>(<=|小于等于|《=))";
    public static final String GEQ_P = "(?<GEQ>(>=|大于等于|》=))";
    public static final String EQL_P = "(?<EQL>(==|恒等于))";
    public static final String NEQ_P = "(?<NEQ>(!=|！=|不等于))";
    public static final String AND_P = "(?<AND>(&&|and|且))";
    public static final String OR_P = "(?<OR>(\\|\\||or|或|｜｜))";
    public static final String NOT_P = "(?<NOT>[!！非])";
    public static final String LSS_P = "(?<LSS>(<|《|小于))";
    public static final String GRE_P = "(?<GRE>(>|》|大于))";
    public static final String ASSIGN_P = "(?<ASSIGN>(=|等于))";
    public static final String SEMICN_P = "(?<SEMICN>[;；])";
    public static final String COMMA_P = "(?<COMMA>[,，])";
    public static final String LPARENT_P = "(?<LPARENT>[(（])";
    public static final String RPARENT_P = "(?<RPARENT>[)）])";
    public static final String LBRACK_P = "(?<LBRACK>[\\[【])";
    public static final String RBRACK_P = "(?<RBRACK>[]】])";
    public static final String LBRACE_P = "(?<LBRACE>[{「『])";
    public static final String RBRACE_P = "(?<RBRACE>[}」』])";
    public static final String IDENFR_P = "(?<IDENFR>[a-zA-Z_\\u4e00-\\u9fa5][a-zA-Z_0-9\\u4e00-\\u9fa5]*)";
    public static final String INTCON_P = "(?<INTCON>[1-9][0-9]*|0)";
    public static final String STRCON_P = "(?<STRCON>[\"“].*?[\"”])";
    public static final String WHITESPACE_P = "(?<WHITESPACE>\\s+)";

    public static final Pattern TOKEN_PATTERN = Pattern.compile(
            MAINTK_P + "|" +
                    CONSTTK_P + "|" +
                    INTTK_P + "|" +
                    BREAKTK_P + "|" +
                    CONTINUETK_P + "|" +
                    IFTK_P + "|" +
                    ELSETK_P + "|" +
                    WHILETK_P + "|" +
                    GETINTTK_P + "|" +
                    PRINTFTK_P + "|" +
                    RETURNTK_P + "|" +
                    VOIDTK_P + "|" +
                    SINGLECOMMENT_P + "|" +
                    MULTICOMMENT_P + "|" +
                    MATPLUS_P + "|" +
                    MATMINU_P + "|" +
                    MATMULT_P + "|" +
                    MATDOTMULT_P + "|" +
                    PLUS_P + "|" +
                    MINU_P + "|" +
                    MULT_P + "|" +
                    DIV_P + "|" +
                    MOD_P + "|" +
                    LEQ_P + "|" +
                    GEQ_P + "|" +
                    EQL_P + "|" +
                    NEQ_P + "|" +
                    AND_P + "|" +
                    OR_P + "|" +
                    NOT_P + "|" +
                    LSS_P + "|" +
                    GRE_P + "|" +
                    ASSIGN_P + "|" +
                    SEMICN_P + "|" +
                    COMMA_P + "|" +
                    LPARENT_P + "|" +
                    RPARENT_P + "|" +
                    LBRACK_P + "|" +
                    RBRACK_P + "|" +
                    LBRACE_P + "|" +
                    RBRACE_P + "|" +
                    IDENFR_P + "|" +
                    INTCON_P + "|" +
                    STRCON_P + "|" +
                    WHITESPACE_P + "|" +
                    "(?<ERR>.)"

    );

    public static boolean eat(Type type) {
        return type == Type.WHITESPACE || type == Type.SINGLECOMMENT || type == Type.MULTICOMMENT;
    }

    private final String stringInfo;
    private final Type type;
    private final int line;

    public Token(int line, Type type, String stringInfo) {
        this.stringInfo = stringInfo;
        this.type = type;
        this.line = line;
    }

    public int line() {
        return line;
    }

    public Type type() {
        return type;
    }

    public String stringInfo() {
        return stringInfo;
    }

    public String toString() {
        return this.type.toString() + " " + this.stringInfo;
    }

}
