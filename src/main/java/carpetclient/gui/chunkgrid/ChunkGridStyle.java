package carpetclient.gui.chunkgrid;

/*
Selection Class for choosing different draw options to apply to the chunk grid debug window.
 */
public enum ChunkGridStyle {
    GRADIENT("Gradient", "Earth's favorite style"),
    GRADIENT_NOBG("Transparent Gradient", "0x's favorite style", false),
    CHECKERBOARD("Checkerboard", "Xcom's favorite style"),
    CHECKERBOARD_NOBG("Transparent Checkerboard", false),
    //FLAT_COLOR("Flat Color"),
    ;

    private final String name;
    private final String desc;
    private final boolean drawBackground;

    ChunkGridStyle(String name, boolean drawBackground) {
        this(name, "", drawBackground);
    }

    ChunkGridStyle(String name, String desc) {
        this(name, desc, true);
    }

    ChunkGridStyle(String name, String desc, boolean drawBackground) {
        this.name = name;
        this.desc = desc;
        this.drawBackground = drawBackground;
    }

    /**
     * Returns if selection is of type gradiant.
     *
     * @return
     */
    public boolean isGradient() {
        return this == GRADIENT || this == GRADIENT_NOBG;
    }

    /**
     * Returns if the selection is of type checkerboard.
     *
     * @return
     */
    public boolean isCheckerboard() {
        return this == CHECKERBOARD || this == CHECKERBOARD_NOBG;
    }

    /*
    public boolean isFlatColor() {
        return this == FLAT_COLOR;
    }
    */

    /**
     * Name of the selection.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Description of the selection.
     *
     * @return
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Returns the default black background or transparent based on selection.
     *
     * @return
     */
    public int getBackgroundColor() {
        return drawBackground ? 0xff000000 : 0;
    }

    /**
     * Selection method to change styles
     */
    public static void changeStyle() {
        GuiChunkGrid.style = values()[(GuiChunkGrid.style.ordinal() + 1) % values().length];
    }
}
