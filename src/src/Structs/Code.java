package Structs;

public class Code {
    private int lineNumber;
    private String first;
    private String second;
    private String third;
    private String fourth;

    public Code(int lineNumber,String first,String second,String third,String fourth){
        this.lineNumber = lineNumber;
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getThird() {
        return third;
    }

    public void setThird(String third) {
        this.third = third;
    }

    public String getFourth() {
        return fourth;
    }

    public void setFourth(String fourth) {
        this.fourth = fourth;
    }

    public String toString(){
        return String.format("%-10d%-10s%-10s%-10s%-10s",lineNumber,first,second,third,fourth);
    }

}
