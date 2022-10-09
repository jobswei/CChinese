package front.nodes;

import front.CompileUnit;

import java.util.HashMap;
import java.util.Map;

public class UnaryExpNode implements ExprNode {
    public enum UnaryOp {
        PLUS, MINU, NOT
    }

    private static final Map<CompileUnit.Type, UnaryOp> TYPE_2_OP = new HashMap<CompileUnit.Type, UnaryOp>() {
        {
            put(CompileUnit.Type.PLUS, UnaryOp.PLUS);
            put(CompileUnit.Type.MINU, UnaryOp.MINU);
            put(CompileUnit.Type.NOT, UnaryOp.NOT);
        }
    };

    private final UnaryOp op;
    private final ExprNode expNode;

    public UnaryExpNode(CompileUnit.Type type, ExprNode exprNode) {
        this.op = TYPE_2_OP.get(type);
        this.expNode = exprNode;
    }

    @Override
    public String toString() {
        return "UnaryExpNode{" +
                "op=" + op +
                ", expNode=" + expNode +
                '}';
    }

    public UnaryOp op() {
        return op;
    }

    public ExprNode expNode() {
        return expNode;
    }
}
