package artcreator.domain;

public enum OutputSize {
    A4(210, 297),
    A3(297, 420),
    A2(420, 594),
    A1(594, 841),
    A0(841, 1189);

    private final int widthMM;
    private final int heightMM;

    OutputSize(int widthMM, int heightMM) {
        this.widthMM = widthMM;
        this.heightMM = heightMM;
    }

    public int getWidthMM() { return widthMM; }
    public int getHeightMM() { return heightMM; }

    public int getPagesRequired() {
        if (this == A4) return 1;
        var area = widthMM * heightMM;
        var a4Area = A4.widthMM * A4.heightMM;
        return (int) Math.ceil((double) area / a4Area);
    }
}
