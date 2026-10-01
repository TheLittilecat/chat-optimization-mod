package minecraft.chat.optimization.mod.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


public final class TlcCommand {

	private static final String PREFIX = "[TheLittle_cat]";

	private TlcCommand() {
	}

	
	public static <S> LiteralArgumentBuilder<S> build(Function<S, CommandFeedback> adapter) {
		return LiteralArgumentBuilder.<S>literal("tlc")
			.executes(ctx -> help(adapter.apply(ctx.getSource())))
			.then(LiteralArgumentBuilder.<S>literal("help")
				.executes(ctx -> help(adapter.apply(ctx.getSource()))))
			.then(LiteralArgumentBuilder.<S>literal("plan")
				.then(LiteralArgumentBuilder.<S>literal("set")
					.then(RequiredArgumentBuilder.<S, String>argument("name", ClientTextArgument.text())
						.suggests((ctx, builder) -> suggestPlanNames(builder))
						.executes(ctx -> planSet(adapter.apply(ctx.getSource()),
							ClientTextArgument.getString(ctx, "name")))))
				.then(LiteralArgumentBuilder.<S>literal("add")
					.then(RequiredArgumentBuilder.<S, String>argument("name", ClientTextArgument.text())
						.executes(ctx -> planAdd(adapter.apply(ctx.getSource()),
							ClientTextArgument.getString(ctx, "name")))))
				.then(LiteralArgumentBuilder.<S>literal("remove")
					.then(RequiredArgumentBuilder.<S, String>argument("name", ClientTextArgument.text())
						.suggests((ctx, builder) -> suggestPlanNames(builder))
						.executes(ctx -> planRemove(adapter.apply(ctx.getSource()),
							ClientTextArgument.getString(ctx, "name"))))))
			.then(LiteralArgumentBuilder.<S>literal("rule")
				.then(LiteralArgumentBuilder.<S>literal("add")
					.then(RequiredArgumentBuilder.<S, String>argument("from", ClientTextArgument.text())
						.then(RequiredArgumentBuilder.<S, String>argument("to", StringArgumentType.greedyString())
							.executes(ctx -> ruleAdd(adapter.apply(ctx.getSource()),
								ClientTextArgument.getString(ctx, "from"),
								StringArgumentType.getString(ctx, "to"))))))
				.then(LiteralArgumentBuilder.<S>literal("remove")
					.then(RequiredArgumentBuilder.<S, String>argument("rule", ClientTextArgument.text())
						.suggests((ctx, builder) -> suggestRuleTexts(builder))
						.executes(ctx -> ruleRemove(adapter.apply(ctx.getSource()),
							ClientTextArgument.getString(ctx, "rule")))))
				.then(LiteralArgumentBuilder.<S>literal("list")
					.executes(ctx -> ruleList(adapter.apply(ctx.getSource())))))
			.then(LiteralArgumentBuilder.<S>literal("last")
				.then(LiteralArgumentBuilder.<S>literal("add")
					.then(RequiredArgumentBuilder.<S, String>argument("text", StringArgumentType.greedyString())
						.executes(ctx -> lastAdd(adapter.apply(ctx.getSource()),
							StringArgumentType.getString(ctx, "text")))))
				.then(LiteralArgumentBuilder.<S>literal("list")
					.executes(ctx -> lastList(adapter.apply(ctx.getSource()))))
				.then(LiteralArgumentBuilder.<S>literal("remove")
					.then(RequiredArgumentBuilder.<S, String>argument("text", ClientTextArgument.text())
						.suggests((ctx, builder) -> suggestSuffixTexts(builder))
						.executes(ctx -> lastRemove(adapter.apply(ctx.getSource()),
							ClientTextArgument.getString(ctx, "text"))))));
	}

	private static int planSet(CommandFeedback fb, String name) {
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		if (!config.setActivePlan(name)) {
			fb.error(PREFIX + ":方案\"" + name + "\"不存在");
			return 1;
		}
		config.save();
		fb.feedback(PREFIX + ":方案已切换至\"" + name + "\"");
		return 1;
	}

	private static int planAdd(CommandFeedback fb, String name) {
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		if (!config.addPlan(name)) {
			fb.error(PREFIX + ":方案\"" + name + "\"已存在");
			return 1;
		}
		config.save();
		fb.feedback(PREFIX + ":\"" + name + "\"方案已添加");
		return 1;
	}

	private static int planRemove(CommandFeedback fb, String name) {
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		if (!config.plans.containsKey(name)) {
			fb.error(PREFIX + ":方案\"" + name + "\"不存在");
			return 1;
		}
		if (!config.removePlan(name)) {
			fb.error(PREFIX + ":至少保留一个方案");
			return 1;
		}
		config.save();
		fb.feedback(PREFIX + ":\"" + name + "\"方案已删除");
		return 1;
	}

	private static int ruleAdd(CommandFeedback fb, String from, String to) {
		if (from == null || from.isEmpty()) {
			fb.error(PREFIX + ":原文本不能为空");
			return 1;
		}
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		ChatReplacerConfig.Plan plan = config.getActivePlan();
		if (plan == null) {
			fb.error(PREFIX + ":当前没有可用方案");
			return 1;
		}
		if (plan.rules.size() >= ChatReplacerConfig.MAX_RULES) {
			fb.error(PREFIX + ":已达最大规则数 " + ChatReplacerConfig.MAX_RULES);
			return 1;
		}
		plan.rules.add(new ChatReplacerConfig.ReplaceRule(from, to));
		config.save();
		fb.feedback(PREFIX + ":已在方案\"" + config.activePlan + "\"添加规则 \"" + from + "\" -> \"" + to
			+ "\" (当前 " + plan.rules.size() + "/" + ChatReplacerConfig.MAX_RULES + ")");
		return 1;
	}

	private static int ruleRemove(CommandFeedback fb, String ruleText) {
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		ChatReplacerConfig.Plan plan = config.getActivePlan();
		if (plan == null) {
			fb.error(PREFIX + ":当前没有可用方案");
			return 1;
		}
		if (!plan.rules.removeIf(rule -> ruleText.equals(rule.from))) {
			fb.error(PREFIX + ":当前方案中未找到规则 \"" + ruleText + "\"");
			return 1;
		}
		config.save();
		fb.feedback(PREFIX + ":已删除规则 \"" + ruleText + "\"");
		return 1;
	}

	private static int ruleList(CommandFeedback fb) {
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		ChatReplacerConfig.Plan plan = config.getActivePlan();
		if (plan == null) {
			fb.error(PREFIX + ":当前没有可用方案");
			return 1;
		}
		fb.feedback(PREFIX + ":当前方案\"" + config.activePlan + "\"的替换规则 (" + plan.rules.size() + "):");
		if (plan.rules.isEmpty()) {
			fb.feedback("  (无规则)");
		} else {
			for (int i = 0; i < plan.rules.size(); i++) {
				ChatReplacerConfig.ReplaceRule rule = plan.rules.get(i);
				fb.feedback("  " + (i + 1) + ". \"" + rule.from + "\" -> \"" + rule.to + "\"");
			}
		}
		return 1;
	}

	private static int lastAdd(CommandFeedback fb, String text) {
		if (text == null || text.isEmpty()) {
			fb.error(PREFIX + ":末尾内容不能为空");
			return 1;
		}
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		ChatReplacerConfig.Plan plan = config.getActivePlan();
		if (plan == null) {
			fb.error(PREFIX + ":当前没有可用方案");
			return 1;
		}
		plan.suffixes.add(text);
		config.save();
		fb.feedback(PREFIX + ":已在方案\"" + config.activePlan + "\"添加末尾规则 \"" + text + "\"");
		return 1;
	}

	private static int lastRemove(CommandFeedback fb, String text) {
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		ChatReplacerConfig.Plan plan = config.getActivePlan();
		if (plan == null) {
			fb.error(PREFIX + ":当前没有可用方案");
			return 1;
		}
		if (!plan.suffixes.removeIf(suffix -> text.equals(suffix))) {
			fb.error(PREFIX + ":当前方案中未找到末尾规则 \"" + text + "\"");
			return 1;
		}
		config.save();
		fb.feedback(PREFIX + ":已删除末尾规则 \"" + text + "\"");
		return 1;
	}

	private static int lastList(CommandFeedback fb) {
		ChatReplacerConfig config = ChatReplacerConfig.INSTANCE;
		ChatReplacerConfig.Plan plan = config.getActivePlan();
		if (plan == null) {
			fb.error(PREFIX + ":当前没有可用方案");
			return 1;
		}
		fb.feedback(PREFIX + ":当前方案\"" + config.activePlan + "\"的末尾追加规则 (" + plan.suffixes.size() + "):");
		if (plan.suffixes.isEmpty()) {
			fb.feedback("  (无末尾规则)");
		} else {
			for (int i = 0; i < plan.suffixes.size(); i++) {
				fb.feedback("  " + (i + 1) + ". \"" + plan.suffixes.get(i) + "\"");
			}
		}
		return 1;
	}

	private static int help(CommandFeedback fb) {
		fb.feedback(PREFIX + ":指令用法:");
		fb.feedback("  /tlc plan set <方案名称>      切换当前使用的方案");
		fb.feedback("  /tlc plan add <方案名称>      添加一个方案");
		fb.feedback("  /tlc plan remove <方案名称>   删除一个方案");
		fb.feedback("  /tlc rule add <原文本> <替换为>  在当前方案添加替换规则");
		fb.feedback("  /tlc rule remove <规则>       删除当前方案中的规则");
		fb.feedback("  /tlc rule list               列出当前方案的全部规则");
		fb.feedback("  /tlc last add <文本>         在当前方案添加末尾追加规则");
		fb.feedback("  /tlc last remove <文本>      删除当前方案中的末尾追加规则");
		fb.feedback("  /tlc last list               列出当前方案的末尾追加规则");
		return 1;
	}

	private static CompletableFuture<Suggestions> suggestPlanNames(SuggestionsBuilder builder) {
		ChatReplacerConfig.INSTANCE.plans.keySet().forEach(builder::suggest);
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestRuleTexts(SuggestionsBuilder builder) {
		ChatReplacerConfig.Plan plan = ChatReplacerConfig.INSTANCE.getActivePlan();
		if (plan != null) {
			for (ChatReplacerConfig.ReplaceRule rule : plan.rules) {
				builder.suggest(rule.from);
			}
		}
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestSuffixTexts(SuggestionsBuilder builder) {
		ChatReplacerConfig.Plan plan = ChatReplacerConfig.INSTANCE.getActivePlan();
		if (plan != null) {
			plan.suffixes.forEach(builder::suggest);
		}
		return builder.buildFuture();
	}

	
	@SuppressWarnings("unused")
	private static <S> S sourceOf(CommandContext<S> context) {
		return context.getSource();
	}
}
