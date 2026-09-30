package minecraft.chat.optimization.mod.core;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public final class ClientTextArgument implements ArgumentType<String> {

	private static final ClientTextArgument INSTANCE = new ClientTextArgument();

	private ClientTextArgument() {
	}

	public static ClientTextArgument text() {
		return INSTANCE;
	}

	public static String getString(CommandContext<?> context, String name) {
		return context.getArgument(name, String.class);
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		reader.skipWhitespace();
		if (!reader.canRead()) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(reader);
		}
		if (reader.peek() == '"') {
			return reader.readQuotedString();
		}
		int start = reader.getCursor();
		while (reader.canRead() && reader.peek() != ' ') {
			reader.skip();
		}
		if (reader.getCursor() == start) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(reader);
		}
		return reader.getString().substring(start, reader.getCursor());
	}

	@Override
	public Collection<String> getExamples() {
		return Arrays.asList("猫娘");
	}
}
