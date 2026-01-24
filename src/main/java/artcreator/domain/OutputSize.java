package artcreator.domain;

public enum OutputSize {
    A4(210, 297),
    A3(297, 420),
    A2(420, 594),
    A1(594, 841),
    A0(841, 1189);

    private final int widthMM, heightMM;

    OutputSize(int widthMM, int heightMM) {
        this.widthMM = widthMM;
        this.heightMM = heightMM;
    }

    public int getWidthMM() { return widthMM; }
    public int getHeightMM() { return heightMM; }

    public int getPagesRequired() {
        return switch (this) {
            case A4 -> 1;
            case A3 -> 2;
            case A2 -> 4;
            case A1 -> 8;
            case A0 -> 16;
        };
    }
}
