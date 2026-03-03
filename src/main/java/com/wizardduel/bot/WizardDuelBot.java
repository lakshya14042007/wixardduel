package com.wizardduel.bot;

import com.wizardduel.game.GameSession;
import com.wizardduel.game.MessageFormatter;
import com.wizardduel.game.TurnResult;
import com.wizardduel.model.Player;
import com.wizardduel.spell.Spell;
import com.wizardduel.spell.SpellRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main bot class – handles all incoming Telegram updates and coordinates game sessions.
 *
 * <p>Supported commands:
 * <ul>
 *   <li>{@code /startduel} – challenge anyone in the group to a duel</li>
 *   <li>{@code /joinduel}  – accept the open challenge</li>
 *   <li>{@code /spell <name>} – cast a spell this round</li>
 *   <li>{@code /status}    – show current HP and cooldowns</li>
 *   <li>{@code /spells}    – list all available spells</li>
 *   <li>{@code /cancelduel} – abandon the current duel (challenger only)</li>
 * </ul>
 */
public class WizardDuelBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(WizardDuelBot.class);

    private final String botUsername;
    private final String botToken;

    /** chatId → GameSession */
    private final Map<Long, GameSession> sessions = new ConcurrentHashMap<>();

    public WizardDuelBot(String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
    }

    // -------------------------------------------------------------------------
    // Telegram API identity
    // -------------------------------------------------------------------------

    @Override public String getBotUsername() { return botUsername; }
    @Override public String getBotToken()    { return botToken; }

    // -------------------------------------------------------------------------
    // Update dispatcher
    // -------------------------------------------------------------------------

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Message msg = update.getMessage();
        long chatId = msg.getChatId();
        long userId = msg.getFrom().getId();
        String firstName = msg.getFrom().getFirstName();
        String text = msg.getText().trim();

        // Strip bot username suffix (e.g. /startduel@MyBot)
        if (text.contains("@")) text = text.substring(0, text.indexOf('@'));

        String[] parts = text.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/startduel"  -> handleStartDuel(chatId, userId, firstName);
            case "/joinduel"   -> handleJoinDuel(chatId, userId, firstName);
            case "/spell"      -> handleSpell(chatId, userId, parts.length > 1 ? parts[1] : "");
            case "/status"     -> handleStatus(chatId, userId);
            case "/spells"     -> send(chatId, "📜 *Available spells:*\n" + MessageFormatter.spellList());
            case "/cancelduel" -> handleCancelDuel(chatId, userId);
            case "/help"       -> send(chatId, helpText());
            default            -> { /* ignore unknown commands */ }
        }
    }

    // -------------------------------------------------------------------------
    // Command handlers
    // -------------------------------------------------------------------------

    private void handleStartDuel(long chatId, long userId, String firstName) {
        if (sessions.containsKey(chatId)) {
            send(chatId, MessageFormatter.alreadyDuel());
            return;
        }
        Player p1 = new Player(firstName, userId);
        GameSession session = new GameSession(chatId, p1);
        sessions.put(chatId, session);
        send(chatId, MessageFormatter.welcomeMessage(firstName));
    }

    private void handleJoinDuel(long chatId, long userId, String firstName) {
        GameSession session = sessions.get(chatId);
        if (session == null) {
            send(chatId, MessageFormatter.noActiveGame());
            return;
        }
        if (session.getState() != GameSession.State.WAITING_FOR_CHALLENGER) {
            send(chatId, "⚠️ The duel is already underway!");
            return;
        }
        if (session.getPlayer1().getUserId() == userId) {
            send(chatId, MessageFormatter.alreadyJoined());
            return;
        }
        Player p2 = new Player(firstName, userId);
        session.setPlayer2(p2);
        send(chatId, MessageFormatter.duelStarted(session.getPlayer1().getName(), firstName));
    }

    private void handleSpell(long chatId, long userId, String spellName) {
        GameSession session = sessions.get(chatId);
        if (session == null || session.getState() == GameSession.State.WAITING_FOR_CHALLENGER) {
            send(chatId, MessageFormatter.noActiveGame());
            return;
        }
        if (session.getState() == GameSession.State.FINISHED) {
            send(chatId, "The duel is over. Use /startduel for a new one!");
            return;
        }
        if (!session.isParticipant(userId)) {
            send(chatId, MessageFormatter.notYourTurn());
            return;
        }
        if (session.hasChosen(userId)) {
            send(chatId, "⏳ You already chose your spell this round. Waiting for opponent…");
            return;
        }
        if (spellName.isBlank()) {
            send(chatId, "Usage: `/spell <n>`\nSpells: attack, fireball, shield, reflect, heal");
            return;
        }

        Spell spell = SpellRegistry.get(spellName.trim().toLowerCase());
        if (spell == null) {
            send(chatId, MessageFormatter.unknownSpell(spellName));
            return;
        }

        // Try to submit the spell (cooldown check happens inside)
        String error = session.submitSpell(userId, spell);
        if (error != null) {
            send(chatId, error);
            return;
        }

        // Acknowledge
        Player caster = session.getPlayer1().getUserId() == userId
                ? session.getPlayer1() : session.getPlayer2();
        Player opponent = (caster == session.getPlayer1()) ? session.getPlayer2() : session.getPlayer1();

        if (!session.bothSpellsChosen()) {
            send(chatId, MessageFormatter.spellAck(caster.getName(), spell.getEmoji(), spell.getName(), opponent.getName()));
            return;
        }

        // Both spells are in — resolve the round
        TurnResult result = session.resolve();
        send(chatId, MessageFormatter.turnResult(result));

        if (result.winner != null) {
            sessions.remove(chatId);
        }
    }

    private void handleStatus(long chatId, long userId) {
        GameSession session = sessions.get(chatId);
        if (session == null || session.getState() == GameSession.State.WAITING_FOR_CHALLENGER) {
            send(chatId, MessageFormatter.noActiveGame());
            return;
        }
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();
        StringBuilder sb = new StringBuilder("📊 *Duel Status – Round " + session.getTurn() + "*\n\n");
        sb.append("❤️ ").append(p1.getName()).append(": ").append(p1.getHpBar()).append("\n");
        sb.append("❤️ ").append(p2.getName()).append(": ").append(p2.getHpBar()).append("\n");
        sb.append("\n").append(MessageFormatter.cooldownStatus(p1.getName(), session.getCooldowns1()));
        sb.append("\n").append(MessageFormatter.cooldownStatus(p2.getName(), session.getCooldowns2()));
        send(chatId, sb.toString());
    }

    private void handleCancelDuel(long chatId, long userId) {
        GameSession session = sessions.get(chatId);
        if (session == null) { send(chatId, MessageFormatter.noActiveGame()); return; }
        if (session.getPlayer1().getUserId() != userId) {
            send(chatId, "❌ Only the challenger can cancel the duel.");
            return;
        }
        sessions.remove(chatId);
        send(chatId, "🚫 Duel cancelled.");
    }

    // -------------------------------------------------------------------------
    // Messaging helper
    // -------------------------------------------------------------------------

    private void send(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        msg.setParseMode("Markdown");
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Help text
    // -------------------------------------------------------------------------

    private String helpText() {
        return """
                🧙 *Wizard Duel Bot – Commands*

                /startduel  – Challenge the group to a wizard duel
                /joinduel   – Accept an open duel challenge
                /spell <n>  – Cast a spell (attack, fireball, shield, reflect, heal)
                /status     – Show current HP and cooldowns
                /spells     – List all available spells with descriptions
                /cancelduel – Cancel the current duel (challenger only)
                /help       – Show this help message

                *How to play:*
                1️⃣ One player uses /startduel in a group chat
                2️⃣ Another player uses /joinduel to accept
                3️⃣ Each round, both players send /spell <n> simultaneously
                4️⃣ Spells are resolved together — first to 0 HP loses!
                """;
    }
}
