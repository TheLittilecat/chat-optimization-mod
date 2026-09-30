package minecraft.chat.optimization.mod.compat;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayerEntity.class)
public class ChatCommandMixin {

	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	private void chatReplacer$handleCommand(String message, CallbackInfo ci) {
		if (message != null && (message.equals("/tlc") || message.startsWith("/tlc "))) {
			ci.cancel();
			TlcCommandDispatcher.handle(message);
		}
	}
}
