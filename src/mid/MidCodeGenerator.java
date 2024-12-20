package mid;

import front.FuncEntry;
import front.SemanticChecker;
import front.SymbolTable;
import front.TableEntry;
import front.nodes.*;
import mid.ircode.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MidCodeGenerator {
    private static boolean optimizer = false;
    private static SymbolTable currentTable = SymbolTable.globalTable();
    private static final Map<String, FuncEntry> FUNC_TABLE = SemanticChecker.getFuncTable();
    private static FuncDef currentFuncDef = null;
    private static BasicBlock currentBasicBlock = null;
    private static int depth = 1;
    private static IrModule IR_MODULE = new IrModule();
    private static String currentBranchLabel = null;
    private static String currentLoopLabel = null;
    private static String currentLoopCond = null;
    private static String currentLoopBody = null;
    private static String currentAfterLoopLabel = null;
    private static String endTag = "_end";

    public static void setOptimizer(boolean optimizer) {
        MidCodeGenerator.optimizer = optimizer;
    }

    public static IrModule compileUnitToIr(CompileUnitNode compileUnitNode) {
        IR_MODULE = new IrModule();
        List<DeclNode> declNodes = compileUnitNode.declNodes();
        List<FuncDefNode> funcDefNodes = compileUnitNode.funcDefNodes();
        FuncDefNode mainFuncDef = compileUnitNode.mainFuncDef();
        for (DeclNode declNode : declNodes) {
            declNodeToIr(declNode);
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            funcDefNodeToIr(funcDefNode);
        }
        funcDefNodeToIr(mainFuncDef);
        return IR_MODULE;
    }

    public static void loadArrayInit(TableEntry tableEntry) {
        List<ExprNode> initValue = tableEntry.initValueList;
        List<Operand> index = new ArrayList<>();
        for (int i = 0; i < tableEntry.getDimension().size(); i++) {
            index.add(new Immediate(0));
        }
        if (optimizer) {
            TableEntry temp = null;
            for (int i = 0; i < initValue.size(); i++) {
                index.add(new Immediate(i));
                if (i == 0) {
                    temp = TempCounter.getTempPointer(tableEntry, index);
                }
                Operand value = expNodeToIr(initValue.get(i));
                if (i == 0) { //TODO: 只有第一个需要element
                    currentBasicBlock.addAfter(new ElementPtr(temp, tableEntry, index));
                } else {
                    currentBasicBlock.addAfter(new BinaryOperator(BinaryOperator.Op.ADD, temp, temp, new Immediate(4)));
                }
                currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, temp, value));
                index.remove(index.size() - 1);
            }
        } else {
            for (int i = 0; i < initValue.size(); i++) {
                index.add(new Immediate(i));
                TableEntry temp = TempCounter.getTempPointer(tableEntry, index);
                Operand value = expNodeToIr(initValue.get(i));
                currentBasicBlock.addAfter(new ElementPtr(temp, tableEntry, index));//TODO: 只有第一个需要element
                currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, temp, value));
                index.remove(index.size() - 1);
            }
        }
    }

    public static void declNodeToIr(DeclNode declNode) {
        List<DefNode> defNodeList = declNode.defNodeList();
        //TODO: 数组定义
        if (!declNode.isConst()) {
            for (DefNode defNode : defNodeList) {
                currentTable.setDefined(defNode.ident());
                TableEntry tableEntry = currentTable.getSymbol(defNode.ident());
                tableEntry.simplify(currentTable);
                if (currentTable.isGlobalTable()) {
                    IR_MODULE.getGlobalVarDefs().add(tableEntry);
                } else {
                    VarDef varDef = new VarDef(tableEntry);
                    currentBasicBlock.addAfter(varDef);
                    if ((tableEntry.refType == TableEntry.RefType.ITEM &&
                            tableEntry.initValue != null)) {
                        Operand init = expNodeToIr(tableEntry.initValue);
                        currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, tableEntry, init));
                    } else if (tableEntry.refType == TableEntry.RefType.ARRAY) {
                        loadArrayInit(tableEntry);
                    }
                }
            }
        } else {
            for (DefNode defNode : defNodeList) {
                currentTable.setDefined(defNode.ident());
                TableEntry tableEntry = currentTable.getSymbolDefined(defNode.ident());
                tableEntry.simplify(currentTable);
                if (tableEntry.refType == TableEntry.RefType.ARRAY) { //常量数组
                    if (currentTable.isGlobalTable()) {
                        IR_MODULE.getGlobalVarDefs().add(tableEntry);

                    } else {
                        VarDef varDef = new VarDef(tableEntry);
                        currentBasicBlock.addAfter(varDef);
                        loadArrayInit(tableEntry);
                    }
                }
            }
        }
    }

    public static void funcDefNodeToIr(FuncDefNode funcDefNode) {
        FuncDef temp = currentFuncDef;

        currentFuncDef = new FuncDef(FUNC_TABLE.get(funcDefNode.name()));
        List<TableEntry> args = currentFuncDef.getFuncEntry().args();
        for (TableEntry tableEntry : args) {
            tableEntry.simplify(currentTable);
        }

        blockNodeToIr(funcDefNode.blockNode());
        IR_MODULE.getFuncDefs().add(currentFuncDef);

        currentFuncDef = temp;
    }

    public static void blockNodeToIr(BlockNode blockNode) {
        List<BlockItemNode> blockItemNodes = blockNode.blockItemNodes();
        //切换环境
        SymbolTable temp = currentTable;
        currentTable = blockNode.getSymbolTable();
        currentFuncDef.addLocalVar(currentTable);
        depth += 1;
        //new basic block
        String label = null;
        if (blockNode.type() == BlockNode.BlockType.BRANCH && currentBranchLabel != null) {
            label = currentBranchLabel;
            currentBranchLabel = null;
        } else if (blockNode.type() == BlockNode.BlockType.LOOP && currentLoopLabel != null) {
            label = currentLoopLabel;
            currentLoopLabel = null;
        }
        if (label == null) {
            label = LabelCounter.getLabel();
        }
        currentBasicBlock = new BasicBlock(label);
        currentFuncDef.addBlock(currentBasicBlock);

        for (BlockItemNode blockItemNode : blockItemNodes) {
            if (blockItemNode instanceof DeclNode) {
                declNodeToIr((DeclNode) blockItemNode);
            } else {
                stmtNodeToIr((StmtNode) blockItemNode);
            }
        }

        //切换环境
        currentTable = temp;
        depth -= 1;
    }

    public static void addNewBasicBlock(String label) {
        currentBasicBlock = new BasicBlock(label);
        currentFuncDef.addBlock(currentBasicBlock);
    }

    public static void stmtNodeToIr(StmtNode stmtNode) {
        if (stmtNode instanceof AssignNode) {
            assignNodeToIr((AssignNode) stmtNode);
        } else if (stmtNode instanceof PrintfNode) {
            printfNodeToIr((PrintfNode) stmtNode);
        } else if (stmtNode instanceof ExprNode) {
            ExprNode exprNode = ((ExprNode) stmtNode).simplify(currentTable);
            expNodeToIr(exprNode);
        } else if (stmtNode instanceof BreakStmtNode) {
            breakStmtNodeToIr((BreakStmtNode) stmtNode);
        } else if (stmtNode instanceof ContinueStmtNode) {
            continueStmtNodeToIr((ContinueStmtNode) stmtNode);
        } else if (stmtNode instanceof BlockNode) {
            blockNodeToIr((BlockNode) stmtNode);
        } else if (stmtNode instanceof WhileNode) {
            whileNodeToIr((WhileNode) stmtNode);
        } else if (stmtNode instanceof IfNode) {
            ifNodeToIr((IfNode) stmtNode);
        } else if (stmtNode instanceof ReturnNode) {
            returnNodeToIr((ReturnNode) stmtNode);
        }
    }

    public static void breakStmtNodeToIr(BreakStmtNode breakStmtNode) {
        currentBasicBlock.addAfter(new Jump(currentAfterLoopLabel));
        addNewBasicBlock(LabelCounter.getLabel());
    }

    public static void continueStmtNodeToIr(ContinueStmtNode continueStmtNode) {
        currentBasicBlock.addAfter(new Jump(currentLoopCond));
        addNewBasicBlock(LabelCounter.getLabel());
    }

    public static WhileNode solveLoopAndOr(WhileNode whileNode) {
        BlockNode whileStmt = new BlockNode(whileNode.whileStmt().blockItemNodes(), BlockNode.BlockType.BRANCH, depth, whileNode.whileStmt().getSymbolTable());
        final IfNode condIf = solveBranchAndOr(new IfNode(whileNode.cond(), whileStmt,
                new BlockNode(new BreakStmtNode(0), BlockNode.BlockType.BRANCH, depth, new SymbolTable(currentTable, "else" + depth))));
        final BlockNode blockedCondIf = new BlockNode(condIf, BlockNode.BlockType.LOOP, depth, new SymbolTable(currentTable, "if" + depth));
        return new WhileNode(new NumberNode(1), blockedCondIf);
    }

    public static void whileNodeToIr(WhileNode whileNode) {
        whileNode.setCond(whileNode.cond().simplify(currentTable));
        if (whileNode.cond() instanceof BinaryExpNode &&
                (((BinaryExpNode) whileNode.cond()).op() == BinaryExpNode.BinaryOp.OR ||
                        ((BinaryExpNode) whileNode.cond()).op() == BinaryExpNode.BinaryOp.AND)) {
            whileNode = solveLoopAndOr(whileNode);
        }
        String afterLoopLabel = LabelCounter.getLabel();
        String whileCondLabel = LabelCounter.getLabel("while_cond");
        String whileBodyLabel = LabelCounter.getLabel("while_body");
        String tempLoopCond = currentLoopCond;
        String tempLoopBody = currentLoopBody;
        String tempAfterLoop = currentAfterLoopLabel;
        currentLoopCond = whileCondLabel;
        currentLoopBody = whileBodyLabel;
        currentAfterLoopLabel = afterLoopLabel;
        //branch
        addNewBasicBlock(whileCondLabel);
        ExprNode simplifiedCond = whileNode.cond().simplify(currentTable);
        Operand dst = expNodeToIr(simplifiedCond);
        currentBasicBlock.addAfter(new Branch(dst, whileBodyLabel,
                afterLoopLabel, Branch.BrOp.BEQ));
        //whileStmt
        currentLoopLabel = whileBodyLabel;
        blockNodeToIr((BlockNode) whileNode.whileStmt());
        //whileStmtEnd
        if (optimizer) {
            dst = expNodeToIr(simplifiedCond);
            currentBasicBlock.addAfter(new Branch(dst, whileBodyLabel, afterLoopLabel, Branch.BrOp.BNE));
        } else {
            currentBasicBlock.addAfter(new Jump(whileCondLabel));
        }

        //currentBasicBlock.setEndLabel(whileBodyLabel + endTag);

        currentLoopCond = tempLoopCond;
        currentLoopBody = tempLoopBody;
        currentAfterLoopLabel = tempAfterLoop;
        addNewBasicBlock(afterLoopLabel);
    }

    public static IfNode solveBranchAndOr(IfNode ifNode) {
        ExprNode cond = ifNode.cond();
        BlockNode ifStmt = ifNode.ifStmt();
        BlockNode elseStmt = ifNode.elseStmt();
        if (!(cond instanceof BinaryExpNode) ||
                ((BinaryExpNode) cond).op() != BinaryExpNode.BinaryOp.AND
                        && ((BinaryExpNode) cond).op() != BinaryExpNode.BinaryOp.OR) {
            return new IfNode(cond, ifStmt, elseStmt);
        } else {
            ExprNode left = ((BinaryExpNode) cond).left();
            ExprNode right = ((BinaryExpNode) cond).right();
            if (((BinaryExpNode) cond).op() == BinaryExpNode.BinaryOp.OR) {
                final IfNode inside = new IfNode(right, ifStmt, elseStmt);
                final IfNode simpledInside = solveBranchAndOr(inside);
                final BlockNode blockInside = new BlockNode(simpledInside, BlockNode.BlockType.BRANCH, depth, new SymbolTable(currentTable, "else" + depth));
                final IfNode outside = new IfNode(left, ifStmt, blockInside);
                return solveBranchAndOr(outside);
            } else {
                final IfNode inside = new IfNode(right, ifStmt, elseStmt);
                final IfNode simpledInside = solveBranchAndOr(inside);
                final BlockNode blockInside = new BlockNode(simpledInside, BlockNode.BlockType.BRANCH, depth, new SymbolTable(currentTable, "if" + depth));
                final IfNode outside = new IfNode(left, blockInside, elseStmt);
                return solveBranchAndOr(outside);
            }
        }
    }

    public static void ifNodeToIr(IfNode ifNode) {
        ifNode.setCond(ifNode.cond().simplify(currentTable));
        ifNode = solveBranchAndOr(ifNode);
        ExprNode simplifiedCond = ifNode.cond();
        Operand dst = expNodeToIr(simplifiedCond);
        String afterIfLabel = LabelCounter.getLabel();
        if (ifNode.elseStmt() != null) {
            String ifLabel = LabelCounter.getLabel("if");
            String elseLabel = LabelCounter.getLabel("else");
            //branch
            currentBasicBlock.addAfter(new Branch(dst, ifLabel, elseLabel, Branch.BrOp.BNE));
            // elseStmt
            currentBranchLabel = elseLabel;
            blockNodeToIr((BlockNode) ifNode.elseStmt());
            currentBasicBlock.addAfter(new Jump(afterIfLabel));
            //currentBasicBlock.setEndLabel(elseLabel + endTag);
            // ifStmt
            currentBranchLabel = ifLabel;
            blockNodeToIr((BlockNode) ifNode.ifStmt());
            //currentBasicBlock.setEndLabel(ifLabel + endTag);
        } else {
            String ifLabel = LabelCounter.getLabel("if");
            //branch
            currentBasicBlock.addAfter(new Branch(dst, ifLabel, afterIfLabel, Branch.BrOp.BEQ));
            //ifStmt
            currentBranchLabel = ifLabel;
            blockNodeToIr((BlockNode) ifNode.ifStmt());
            //currentBasicBlock.setEndLabel(ifLabel + endTag);
        }
        addNewBasicBlock(afterIfLabel);
    }

    public static void assignNodeToIr(AssignNode assignNode) {
        LValNode left = assignNode.lVal();
        TableEntry dst = currentTable.getSymbolDefined(left.ident());
        ExprNode right = assignNode.exprNode().simplify(currentTable);
        if (right instanceof GetintNode) {
            Operand value = getIntNodeToIr((GetintNode) right);
            if (dst.refType == TableEntry.RefType.ARRAY || dst.refType == TableEntry.RefType.POINTER) {
                dst = getElementPointer(dst, assignNode.lVal());
            }
            currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, dst, value));
        } else {
            Operand value = expNodeToIr(right);
            if (dst.refType == TableEntry.RefType.ARRAY || dst.refType == TableEntry.RefType.POINTER) {
                dst = getElementPointer(dst, assignNode.lVal());
            }
            currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, dst, value));
        }
    }

    public static Operand expNodeToIr(ExprNode exprNode) {
        if (exprNode instanceof BinaryExpNode) {
            return binaryExpNodeToIr((BinaryExpNode) exprNode);
        } else if (exprNode instanceof UnaryExpNode) {
            return unaryExpNodeToIr((UnaryExpNode) exprNode);
        } else if (exprNode instanceof GetintNode) {
            return getIntNodeToIr((GetintNode) exprNode);
        } else if (exprNode instanceof FuncCallNode) {
            return funcCallNodeToIr((FuncCallNode) exprNode);
        } else if (exprNode instanceof LValNode) {
            return LValNodeToIr((LValNode) exprNode);
        } else if (exprNode instanceof NumberNode) {
            return new Immediate(((NumberNode) exprNode).number());
        } else {
            return null;
        }
    }

    public static Operand binaryExpNodeToIr(BinaryExpNode exprNode) {
        Operand left = expNodeToIr(exprNode.left());
        Operand right = expNodeToIr(exprNode.right());
        TableEntry dst = TempCounter.getTemp();
        currentBasicBlock.addAfter(new BinaryOperator(exprNode.op(), dst, left, right));
        return dst;
    }

    public static Operand unaryExpNodeToIr(UnaryExpNode exprNode) {
        Operand right = expNodeToIr(exprNode.expNode());
        if (exprNode.op() != UnaryExpNode.UnaryOp.PLUS) {
            TableEntry dst = TempCounter.getTemp();
            currentBasicBlock.addAfter(new UnaryOperator(exprNode.op(), dst, right));
            return dst;
        } else {
            return right;
        }
    }

    public static Operand getIntNodeToIr(GetintNode getintNode) {
        TableEntry dst = TempCounter.getTemp();
        currentBasicBlock.addAfter(new Input(dst));
        return dst;
    }

    public static Operand funcCallNodeToIr(FuncCallNode funcCallNode) {
        FuncEntry funcEntry = FUNC_TABLE.get(funcCallNode.ident());
        TableEntry dst = funcEntry.returnType() == TableEntry.ValueType.INT ?
                TempCounter.getTemp() : null;
        List<Operand> irArgs = new ArrayList<>();
        List<ExprNode> args = funcCallNode.args();
        for (ExprNode exprNode : args) {
            ExprNode simplifyedExpr = exprNode.simplify(currentTable);
            irArgs.add(expNodeToIr(simplifyedExpr));
        }
        if (funcEntry.returnType() == TableEntry.ValueType.VOID) {
            currentBasicBlock.addAfter(new Call(funcEntry, irArgs));
        } else {
            currentBasicBlock.addAfter(new Call(funcEntry, irArgs, dst));
        }
        return dst;
    }

    public static TableEntry getElementPointer(TableEntry base, LValNode lValNode) {
        List<Operand> index = new ArrayList<>();
        if (base.refType == TableEntry.RefType.ARRAY) {
            index.add(new Immediate(0));
        }
        for (ExprNode exprNode : lValNode.index()) {
            ExprNode simplifiedExpr = exprNode.simplify(currentTable);
            index.add(expNodeToIr(simplifiedExpr));
        }
        TableEntry temp = TempCounter.getTempPointer(base, index);
        currentBasicBlock.addAfter(new ElementPtr(temp, base, index));
        return temp;
    }

    public static Operand LValNodeToIr(LValNode lValNode) {
        // TODO: 数组调用
        TableEntry dst = null;
        TableEntry src = currentTable.getSymbolDefined(lValNode.ident());
        if (src.refType == TableEntry.RefType.ITEM) {
            dst = TempCounter.getTemp();
            currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.LOAD, dst, src));
        } else if (src.refType == TableEntry.RefType.ARRAY ||
                src.refType == TableEntry.RefType.POINTER) {
            TableEntry temp = getElementPointer(src, lValNode);
            if (lValNode.dimension == 0) {
                dst = TempCounter.getTemp();
                currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.LOAD, dst, temp));
            } else {
                dst = temp;
            }
        }
        return dst;
    }

    public static void returnNodeToIr(ReturnNode returnNode) {
        Operand returnValue;
        if (returnNode.returnExpr() != null) {
            ExprNode returnExpr = returnNode.returnExpr().simplify(currentTable);
            returnValue = expNodeToIr(returnExpr);
            currentBasicBlock.addAfter(new Return(returnValue));
        } else {
            currentBasicBlock.addAfter(new Return(null));
        }

    }

    public static void printfNodeToIr(PrintfNode printfNode) {
        String formatString = printfNode.formatString();
        List<String> formatStrings = Arrays.stream((formatString.split("(?<=%d)|(?<=%整)|(?=%d)|(?=%整)")))
                .collect(Collectors.toList());
        int putIntCnt = 0;
        List<ExprNode> args = printfNode.args();
        List<Operand> results = new ArrayList<>();
        for (ExprNode arg : args) {
            results.add(expNodeToIr(arg.simplify(currentTable)));
        }
        for (String string : formatStrings) {
            if (!string.equals("%d") && !string.equals("%整")) {
                String label = StringCounter.findString(string);
                currentBasicBlock.addAfter(new PrintStr(label, string));
            } else {
                Operand result = results.get(putIntCnt);
                currentBasicBlock.addAfter(new PrintInt(result));
                putIntCnt += 1;
            }
        }
    }
}
