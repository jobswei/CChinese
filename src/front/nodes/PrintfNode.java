package front.nodes;

import exception.CompileExc;
import front.SymbolTable;
import front.TableEntry;

import java.util.List;

public class PrintfNode extends FuncCallNode {
    private final String formatString;

    public PrintfNode(int line, String formatString, List<ExprNode> args) {
        super("printf", line, args, TableEntry.ValueType.VOID);
        formatString = formatString.replace("\"", "");
        formatString = formatString.replace("“", "");
        this.formatString = formatString.replace("”", "");
    }

    public String formatString() {
        return formatString;
    }

    public void checkArgNum() throws CompileExc {
        if (("\"" + formatString + "\"").split("%d").length - 1 != super.args().size() &&
                ("\"" + formatString + "\"").split("%整").length - 1 != super.args().size()) {
            throw new CompileExc(CompileExc.ErrType.FORMAT_VAR_ERR, super.line());
        }
    }

    public void checkFormatString() throws CompileExc {
        for (int i = 0; i < formatString.length(); i++) {
            int asc = (int) formatString.charAt(i);
            if (asc == 92 && (i == formatString.length() - 1 || formatString.charAt(i + 1) != 'n')) {
                throw new CompileExc(CompileExc.ErrType.ILLEGAL_CHAR, super.line());
            }
//            if (asc == 37 && (i == formatString.length() - 1 || formatString.charAt(i + 1) != 'd') ||
//                    asc == 37 && (i == formatString.length() - 1 || formatString.charAt(i + 1) != '整')) {
//                throw new CompileExc(CompileExc.ErrType.ILLEGAL_CHAR, super.line());
//            }
//            if (!(asc == 32 || asc == 33 || asc == 37 || (asc >= 40 && asc <= 126))) {
//                throw new CompileExc(CompileExc.ErrType.ILLEGAL_CHAR, super.line());
//            }
        }
    }

    @Override
    public String toString() {
        return "PrintfNode{\n" +
                super.toString() +
                "formatString='" + formatString + '\'' +
                "\n}";
    }

    @Override
    public ExprNode simplify(SymbolTable symbolTable) {
        return this;
    }
}
