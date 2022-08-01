package tf.ssf.sfort.operate.client;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class BitStakScreen extends Screen {
    protected float sidebarScrollTarget;
    protected float sidebarScroll;
    protected float sidebarHeight;
    static String[] hlp = new String[]{
            "while the computer is off you can put items into it to program it,",
            "it will turnoff once it's done executing, to turn it on give it a redstone signal.",
            "the stack can only hold 6 numbers any more and the computer will crash",
            "crashing the computer causes it to explode",
            "",
            "Item constants:",
            "\tCharcoal: -1",
            "\tGlass: 0",
            "\tWhite Dye: 1",
            "\tWhite Wool: 1",
            "\tOrange Dye: 2",
            "\tOrange Wool: 2",
            "\tMagenta Dye: 3",
            "\tMagenta Wool: 4",
            "\tLight Blue Dye: 4",
            "\tYellow Dye: 5",
            "\tLime Dye: 6",
            "\tPink Dye: 7",
            "\tGrey Dye: 8",
            "\tLight blue Wool: 8",
            "\tLight Grey Dye: 9",
            "\tCyan Dye: 10",
            "\tPurple Dye:  11",
            "\tBlue Dye: 12",
            "\tBrown Dye: 13",
            "\tGreen Dye: 14",
            "\tRed Dye: 15",
            "\tBlack Dye: 16",
            "\tYellow Wool: 16",
            "\tLime Wool: 32",
            "\tPink Wool: 64",
            "\tGrey Wool: 128",
            "\tLight Grey Wool: 256",
            "\tCyan Wool: 512",
            "\tPurple Wool: 1024",
            "\tBlue Wool: 2048",
            "\tBrows Wool: 4096",
            "\tGreen Wool: 8192",
            "\tRed Wool: 16384",
            "\tBlack Wool: 32768",
            "",
            "",
            "Item instructions:",
            "\tRedstone - adds together last 2 values on the stack","",
            "\tStick - subtracts last 2 values on the stack","",
            "\tIron Ingot - compares last two values with > returning 0 when true and -1 when not","",
            "\tCopper Ingot - compares last two values with < returning 0 when true and -1 when not","",
            "\tLapis Lauzli - divides last 2 values on the stack","",
            "\tBone - multiplies last 2 values on the stack","",
            "\tGlass Bottle - bitwise & the last 2 values on the stack","",
            "\tFurnace - bitwise ^ the last 2 values on the stack","",
            "\tRedstone Torch - if the last value on the stack is 0 return -1 otherwise returns 0","",
            "\tTorch - if the last 2 values are equal returns 0 otherwise -1","",
            "\tCobblestone - duplicates the last value on the stack","",
            "\tGunpowder - removes the last value on the stack","",
            "\tRepeater - takes the last value on the stack and delays computation for that long","",
            "\tFeather - puts the current instruction position onto the stack","",
            "\tLever - takes the last value off the stack and resumes code execution from that item","",
            "\tComparator - takes the last value off the stack and skips one instruction if it's not 0","",
            "\tAmethyst Shard - swaps the last 2 values on the stack","",
            "\tBowl - sets the redstone output to the last value on the stack","",
            "\tQuartz - adds the current redstone value to the stack","",
            "\tSugar - bitwise shift << a value off the stack to the left by the amount of the last value off the stack","",
            "\tSpider Eye - bitwise shift >> a value off the stack to the right by the amount of the last value off the stack","",
            "\tBlaze Powder - gets the color signals from connected color tubes and adds it to the stack (see Color IO)","",
            "\tBrick - Increase the connected color tubes strength for the relevant colors (see Color IO)","",
            "\tFlint - Lower the connected color tubes strength for the relevant colors (see Color IO)","",
            "\tPaper - takes a color bit and returns the signal strength of the relevant color","",
            "\tIce - do nothing",
            "",
            "",
            "Color IO",
            "dye constants match up with the corresponding integer bit",
            "so for example a signal of white, orange and light blue would be: 1+2+8 = 11 or 0000000000000001011 in binary",
            "below is also a conversion to decimal for convenience:",
            "White: 1",
            "Orange: 2",
            "Magenta: 4",
            "Light blue: 8",
            "Yellow: 16",
            "Lime: 32",
            "Pink: 64",
            "Grey: 128",
            "Light Grey: 256",
            "Cyan: 512",
            "Purple: 1024",
            "Blue: 2048",
            "Brows: 4096",
            "Green: 8192",
            "Red: 16384",
            "Black: 32768",
            ""
    };
    public BitStakScreen() {
        super(Text.of("Bit Stak Computer Help"));
    }
    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        sidebarScrollTarget -= amount*20;
        return super.mouseScrolled(x, y, amount);
    }
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        fill(matrix, 0, 0, width, height, 0x44000000);
        float scroll = sidebarHeight < height ? 0 : sidebarScroll;
        sidebarHeight = 20;
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        float y = 22-scroll;
        for (String h : hlp) {
            int x = 16;
            for (String word : Splitter.on(CharMatcher.whitespace()).split(h)) {
                if (textRenderer.getWidth(word) + x > width) {
                    x = 16;
                    y += 12;
                    sidebarHeight += 12;
                }
                if (y < 4) continue;
                x = textRenderer.drawWithShadow(matrix, word + " ", x, y, 0xffffc121);
            }
            sidebarHeight+=20;
            y+=20;
        }
        textRenderer.drawWithShadow(matrix, "|", 6, (scroll/(sidebarHeight-height))*(height-20)+5, -1);
    }
    @Override
    public void tick() {
        if (sidebarHeight > height) {
            sidebarScroll += (sidebarScrollTarget-sidebarScroll)/2;
            if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
            float h = sidebarHeight-height;
            if (sidebarScrollTarget > h) sidebarScrollTarget = h+((sidebarScrollTarget-h)/2);
        }
    }
}
