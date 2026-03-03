package com.wizardduel.game;

import com.wizardduel.model.Player;
import com.wizardduel.spell.SpellRegistry;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Formats game events into Telegram MarkdownV2-compatible strings.
 *
 * <p>We use standard Markdown (parse_mode = Markdown) rather than MarkdownV2 to keep
 * the escaping simple — bold via *text* and italic via _text_.
 */
public final class MessageFormatter {

    private MessageFormatter() {}

    // -----------------------------------------------------------------------
    // Static messages
    // -----------------------------------------------------------------------

    public static String welcomeMessage(String p1Name) {
        return "🧙 *Wizard Duel!*\n\n"
                + p1Name + " wants to duel!\n"
                + "Another player must use /joinduel to accept the challenge.\n\n"
                + "📜 *Spells:*\n" + spellList();
    }

    public static String duelStarted(String p1Name, String p2Name) {
        return "⚔️ *Duel started: " + p1Name + " vs " + p2Name + "!*\n"
                + "Both wizards start with 100 HP.\n\n"
                + "📜 *Spells:*\n" + spellList() + "\n\n"
                + "🎯 *Round 1 – cast your spells with* `/spell <name>`";
    }

    public static String spellList() {
        StringBuilder sb = new StringBuilder();
        SpellRegistry.all().forEach(s -> sb.append("  ").append(s.getDescription()).append("\n"));
        return sb.toString().stripTrailing();
    }

    // -----------------------------------------------------------------------
    // Turn messages
    // -----------------------------------------------------------------------

    public static String spellAck(String playerName, String spellEmoji, String spellName, String waitingFor) {
        return "✅ " + playerName + " chose " + spellEmoji + " *" + spellName + "*. "
                + "Waiting for " + waitingFor + "…";
    }

    public static String turnResult(TurnResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("💥 *Resolving Round ").append(r.turn).append("…*\n\n");

        // What each player cast
        sb.append(r.p1Name).append(" ➜ ").append(r.p1SpellEmoji).append(" *").append(r.p1SpellName).append("*\n");
        sb.append(r.p2Name).append(" ➜ ").append(r.p2SpellEmoji).append(" *").append(r.p2SpellName).append("*\n\n");

        // Narrative
        sb.append("_Effects:_\n");

        if (r.p1Healed > 0) sb.append("  💚 ").append(r.p1Name).append(" healed ").append(r.p1Healed).append(" HP\n");
        if (r.p2Healed > 0) sb.append("  💚 ").append(r.p2Name).append(" healed ").append(r.p2Healed).append(" HP\n");

        if (r.p1DamageDealt > 0) sb.append("  ").append(r.p1SpellEmoji)
                .append(" ").append(r.p1Name).append(" dealt ").append(r.p1DamageDealt).append(" dmg to ").append(r.p2Name).append("\n");
        if (r.p2DamageDealt > 0) sb.append("  ").append(r.p2SpellEmoji)
                .append(" ").append(r.p2Name).append(" dealt ").append(r.p2DamageDealt).append(" dmg to ").append(r.p1Name).append("\n");

        if (r.p1ReflectedDamage > 0)
            sb.append("  🔮 ").append(r.p2Name).append(" reflected ").append(r.p1ReflectedDamage).append(" dmg back at ").append(r.p1Name).append("\n");
        if (r.p2ReflectedDamage > 0)
            sb.append("  🔮 ").append(r.p1Name).append(" reflected ").append(r.p2ReflectedDamage).append(" dmg back at ").append(r.p2Name).append("\n");

        // HP bars
        sb.append("\n❤️ *HP:*\n");
        sb.append("  ").append(r.p1Name).append(": ").append(hpBar(r.p1HpAfter)).append("\n");
        sb.append("  ").append(r.p2Name).append(": ").append(hpBar(r.p2HpAfter)).append("\n");

        // Cooldowns (only show non-zero)
        StringJoiner cdLines = new StringJoiner("\n  ", "  ", "");
        cdLines.setEmptyValue("");
        appendCd(cdLines, r.p1Name, "Fireball", r.p1FireballCooldown);
        appendCd(cdLines, r.p1Name, "Shield",   r.p1ShieldCooldown);
        appendCd(cdLines, r.p1Name, "Reflect",  r.p1ReflectCooldown);
        appendCd(cdLines, r.p2Name, "Fireball", r.p2FireballCooldown);
        appendCd(cdLines, r.p2Name, "Shield",   r.p2ShieldCooldown);
        appendCd(cdLines, r.p2Name, "Reflect",  r.p2ReflectCooldown);
        String cdStr = cdLines.toString();
        if (!cdStr.isBlank()) sb.append("\n⏳ *Cooldowns:*\n").append(cdStr).append("\n");

        // Winner?
        if (r.winner != null) {
            sb.append("\n");
            if ("draw".equals(r.winner)) {
                sb.append("🤝 *It's a draw!* Both wizards fell at the same time.");
            } else {
                sb.append("🏆 *").append(r.winner).append(" wins the duel!* 🎉");
            }
        } else {
            sb.append("\n🎯 *Round ").append(r.turn + 1).append("* – cast your spells with `/spell <name>`");
        }

        return sb.toString();
    }

    public static String cooldownStatus(String playerName, Map<String, Integer> cds) {
        StringBuilder sb = new StringBuilder("⏳ *Cooldowns for " + playerName + ":*\n");
        boolean any = false;
        for (Map.Entry<String, Integer> e : cds.entrySet()) {
            if (e.getValue() > 0) {
                sb.append("  ").append(e.getKey()).append(" → ").append(e.getValue()).append(" turn(s)\n");
                any = true;
            }
        }
        if (!any) sb.append("  All spells are ready! ✅");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Errors / misc
    // -----------------------------------------------------------------------

    public static String noActiveGame()  { return "❌ No active duel in this chat. Use /startduel to begin!"; }
    public static String alreadyDuel()   { return "⚠️ A duel is already in progress in this chat."; }
    public static String alreadyJoined() { return "⚠️ You already started this duel!"; }
    public static String unknownSpell(String name) {
        return "❓ Unknown spell: *" + name + "*\nAvailable: attack, fireball, shield, reflect, heal";
    }
    public static String notYourTurn()   { return "❌ You are not part of the current duel."; }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String hpBar(int hp) {
        int max = 100;
        int filled = (int) Math.round((hp / (double) max) * 10);
        return "█".repeat(Math.max(0, filled)) + "░".repeat(Math.max(0, 10 - filled)) + " " + hp + "/" + max;
    }

    private static void appendCd(StringJoiner j, String name, String spell, int turns) {
        if (turns > 0) j.add(name + "'s " + spell + " → " + turns + " turn(s)");
    }
}
