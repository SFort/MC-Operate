package tf.ssf.sfort.operate.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.operate.MainClient;
import tf.ssf.sfort.operate.client.FakeRequestScreen;

@Mixin(Keyboard.class)
public class MixinKeyboard {

	@Shadow @Final private MinecraftClient client;

	@Inject(method="onKey(JIIII)V", at=@At("HEAD"))
	public void operateRequestInputHead(CallbackInfo ci) {
		if (client.currentScreen == null)
			client.currentScreen = MainClient.getRequestScreen();
	}

	@Inject(method="onKey(JIIII)V", at=@At("TAIL"))
	public void operateRequestInputTail(CallbackInfo ci) {
		if (client.currentScreen instanceof FakeRequestScreen)
			client.currentScreen = null;
	}

	@ModifyVariable(method="onChar(JII)V", at=@At("STORE"))
	public Element operateRequestInput(Element in) {
		if (in != null) return in;
		return MainClient.getRequestScreen();
	}

}
