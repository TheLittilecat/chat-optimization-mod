package minecraft.chat.optimization.mod.compat;

import minecraft.chat.optimization.mod.core.CommandFeedback;
import minecraft.chat.optimization.mod.core.TlcCommand;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.LiteralText;


public final class PlatformImpl implements Platform {

	@Override
	public void registerChatHook() {
		
	}

	@Override
	public void registerCommands() {
		ClientCommandManager.DISPATCHER.register(TlcCommand.build(PlatformImpl::adapt));
	}

	private static CommandFeedback adapt(FabricClientCommandSource source) {
		return new CommandFeedback() {
			@Override
			public void feedback(String message) {
				source.sendFeedback(new LiteralText(message));
			}

			@Override
			public void error(String message) {
				source.sendError(new LiteralText(message));
			}
		};
	}
}
