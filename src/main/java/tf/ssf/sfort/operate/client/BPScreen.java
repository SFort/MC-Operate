package tf.ssf.sfort.operate.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class BPScreen extends Screen {
    public BPScreen(Text name) {
        super(name);
    }

    protected void init() {
    }

    public void removed() {
        McClient.mc.options.keyUse.setPressed(false);
    }

    public void tick() {
        //((AccessKey)mc.options.keyUse).getKey().getCode()
        if(!InputUtil.isKeyPressed(McClient.mc.getWindow().getHandle(), 1)){onClose();}
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }

    public boolean isPauseScreen() {
        return false;
    }
}
