package carpetclient.gui.chunkgrid;

public enum ChunkGridStyle {
    GRADIENT("Gradient", "Earth's favorite style"),
    GRADIENT_NOBG("Transparent Gradient", "0x's favorite style", false),
    CHECKERBOARD("Checkerboard"),
    CHECKERBOARD_NOBG("Transparent Checkerboard", "Xcom's favorite style", false),
    //FLAT_COLOR("Flat Color"),
    ;

    private final String name;
    private final String desc;
    private final boolean drawBackground;

    ChunkGridStyle(String name) {
        this(name, null);
    }

    ChunkGridStyle(String name, String desc) {
        this(name, desc, true);
    }

    ChunkGridStyle(String name, String desc, boolean drawBackground) {
        this.name = name;
        this.desc = desc;
        this.drawBackground = drawBackground;
    }

    public boolean isGradient() {
        return this == GRADIENT || this == GRADIENT_NOBG;
    }

    public boolean isCheckerboard() {
        return this == CHECKERBOARD || this == CHECKERBOARD_NOBG;
    }

    /*
    public boolean isFlatColor() {
        return this == FLAT_COLOR;
    }
    */

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getBackgroundColor() {
        return drawBackground ? 0xff000000 : 0;
    }
}
