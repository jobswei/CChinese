package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Srl extends MipsInstr {
    private final int rd;
    private final int rs;
    private final int imm;

    public Srl(int rd, int rs, int imm) {
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    public Srl(String label, int rd, int rs, int imm) {
        super.setLabel(label);
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    public int getImm() {
        return imm;
    }

    public int getRd() {
        return rd;
    }

    public int getRs() {
        return rs;
    }

    @Override
    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "srl " + "$" + RF.ID_TO_NAME.get(rd) + ", " +
                "$" + RF.ID_TO_NAME.get(rs) + ", " + imm;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
