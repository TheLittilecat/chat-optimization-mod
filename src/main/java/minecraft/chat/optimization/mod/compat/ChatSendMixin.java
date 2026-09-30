package minecraft.chat.optimization.mod.compat;

import minecraft.chat.optimization.mod.core.ChatReplacer;
import minecraft.chat.optimization.mod.core.ChatReplacerConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(ClientPlayerEntity.class)
public class ChatSendMixin {

	@ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private String chatReplacer$modifyMessage(String message) {
		
		
		if (message == null || message.startsWith("/")) {
			return message;
		}
		return ChatReplacer.apply(ChatReplacerConfig.INSTANCE, message);
	}
}
