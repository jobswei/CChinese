package front.nodes;

public class BreakStmtNode implements StmtNode {
    private final int line;

    public BreakStmtNode(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "BreakStmtNode{" +
                "line=" + line +
                '}';
    }
}
