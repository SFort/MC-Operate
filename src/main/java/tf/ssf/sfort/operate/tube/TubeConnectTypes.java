package tf.ssf.sfort.operate.tube;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

public enum TubeConnectTypes {
    WHITE("white", DyeColor.WHITE),
    ORANGE("orange", DyeColor.ORANGE),
    MAGENTA("magenta", DyeColor.MAGENTA),
    LIGHT_BLUE("light_blue", DyeColor.LIGHT_BLUE),
    YELLOW("yellow", DyeColor.YELLOW),
    LIME("lime", DyeColor.LIME),
    PINK("pink", DyeColor.PINK),
    GREY("grey", DyeColor.GRAY),
    LIGHT_GREY("light_grey", DyeColor.LIGHT_GRAY),
    CYAN("cyan", DyeColor.CYAN),
    PURPLE("purple", DyeColor.PURPLE),
    BLUE("blue", DyeColor.BLUE),
    BROWN("brown", DyeColor.BROWN),
    GREEN("green", DyeColor.GREEN),
    RED("red", DyeColor.RED),
    BLACK("black", DyeColor.BLACK),
    NONE("none"),
    ALL("all");
    public final long mask;
    public final DyeColor color;
    public final String name;
    public final float red;
    public final float green;
    public final float blue;
    public final float renderX;
    public final float renderZ;
    public final int compOne;
    public final long one;
    public final int shift;
    public static final ImmutableMap<Item, TubeConnectTypes> itemMap;
    public static final ImmutableMap<String, TubeConnectTypes> nameMap;

    static {
        ImmutableMap.Builder<Item, TubeConnectTypes> bldr = new ImmutableMap.Builder<>();
        bldr.put(Items.WHITE_DYE, WHITE);
        bldr.put(Items.ORANGE_DYE, ORANGE);
        bldr.put(Items.MAGENTA_DYE, MAGENTA);
        bldr.put(Items.LIGHT_BLUE_DYE, LIGHT_BLUE);
        bldr.put(Items.YELLOW_DYE, YELLOW);
        bldr.put(Items.LIME_DYE, LIME);
        bldr.put(Items.PINK_DYE, PINK);
        bldr.put(Items.GRAY_DYE, GREY);
        bldr.put(Items.LIGHT_GRAY_DYE, LIGHT_GREY);
        bldr.put(Items.CYAN_DYE, CYAN);
        bldr.put(Items.PURPLE_DYE, PURPLE);
        bldr.put(Items.BLUE_DYE, BLUE);
        bldr.put(Items.BROWN_DYE, BROWN);
        bldr.put(Items.GREEN_DYE, GREEN);
        bldr.put(Items.RED_DYE, RED);
        bldr.put(Items.BLACK_DYE, BLACK);
        itemMap = bldr.build();
        ImmutableMap.Builder<String, TubeConnectTypes> nameBldr = new ImmutableMap.Builder<>();
        for (TubeConnectTypes con : TubeConnectTypes.values()) {
            nameBldr.put(con.name, con);
        }
        nameMap = nameBldr.build();
    }

    TubeConnectTypes(String name) {
        this.mask = 0;
        this.color = null;
        this.name = name;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.renderX = 0;
        this.renderZ = 0;
        this.compOne = 0;
        this.one = 0;
        this.shift = 0;
    }

    TubeConnectTypes(String name, DyeColor color) {
        this.compOne = 1 << ordinal();
        this.shift = ordinal() * 4;
        this.one = 1L << shift;
        this.mask = one + (one << 1) + (one << 2) + (one << 3);
        this.color = color;
        this.name = name;
        int scolor = color.getSignColor();
        this.red = ((scolor >> 16) & 0xff) / 255f;
        this.green = ((scolor >> 8) & 0xff) / 255f;
        this.blue = (scolor & 0xff) / 255f;
        this.renderZ = (float) (ordinal() / 4) * .06f - .09f;
        this.renderX = (ordinal() % 4) * .06f - .09f;
    }

    public int getStrength(long colorLvl) {
        return (int) ((colorLvl & mask) >>> shift);
    }

    public TubeConnectTypes next() {
        return values()[ordinal() + 1 >= TubeConnectTypes.values().length ? 0 : ordinal() + 1];
    }

    public static int compressColor(long color) {
        int ret = 0;
        for (TubeConnectTypes con : TubeConnectTypes.values()) {
            if ((color & con.mask) != 0) ret |= 1 << con.ordinal();
        }
        return ret;
    }
}
