package edu.umbc.cs.maple.hiergen.CAT;

public class CausalEdge {

    private int start, end;
    private String relavantVariable;

    public CausalEdge() {
        // for de/serialization
    }

    public CausalEdge(int start, int end, String var) {
        this.start = start;
        this.end = end;
        this.relavantVariable = var;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getRelevantVariable() {
        return relavantVariable;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getRelavantVariable() {
        return relavantVariable;
    }

    public void setRelavantVariable(String relavantVariable) {
        this.relavantVariable = relavantVariable;
    }
}
