package com.wizardduel.game;

import com.wizardduel.model.Player;
import com.wizardduel.spell.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the complete state of a single Wizard Duel and contains the battle engine.
 *
 * <p>Cooldowns are tracked per-player, per-spell-name in {@code cooldowns1} / {@code cooldowns2}.
 * A value of 0 means "ready to cast".
 */
public class GameSession {

    public enum State { WAITING_FOR_CHALLENGER, IN_PROGRESS, FINISHED }

    private final long chatId;
    private final Player player1;
    private Player player2;               // set when challenger joins
    private State state;
    private int turn;

    private Spell chosenSpell1;
    private Spell chosenSpell2;

    // cooldown[spellName] = turns remaining (0 = ready)
    private final Map<String, Integer> cooldowns1 = new HashMap<>();
    private final Map<String, Integer> cooldowns2 = new HashMap<>();

    public GameSession(long chatId, Player player1) {
        this.chatId = chatId;
        this.player1 = player1;
        this.state = State.WAITING_FOR_CHALLENGER;
        this.turn = 1;
    }

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    public void setPlayer2(Player p2) {
        this.player2 = p2;
        this.state = State.IN_PROGRESS;
    }

    // -------------------------------------------------------------------------
    // Spell submission
    // -------------------------------------------------------------------------

    /**
     * Try to assign a spell for a player.
     *
     * @return null on success, or an error message to send back privately
     */
    public String submitSpell(long userId, Spell spell) {
        if (state != State.IN_PROGRESS) return "No active duel in this chat.";

        boolean isP1 = player1.getUserId() == userId;
        boolean isP2 = player2 != null && player2.getUserId() == userId;

        if (!isP1 && !isP2) return "You are not part of this duel!";

        Map<String, Integer> cd = isP1 ? cooldowns1 : cooldowns2;
        int remaining = cd.getOrDefault(spell.getName().toLowerCase(), 0);
        if (remaining > 0) {
            return spell.getEmoji() + " *" + spell.getName() + "* is on cooldown for " + remaining + " more turn(s).";
        }

        if (isP1) {
            if (chosenSpell1 != null) return "You already submitted a spell this round!";
            chosenSpell1 = spell;
        } else {
            if (chosenSpell2 != null) return "You already submitted a spell this round!";
            chosenSpell2 = spell;
        }
        return null; // success
    }

    public boolean bothSpellsChosen() {
        return chosenSpell1 != null && chosenSpell2 != null;
    }

    public boolean hasChosen(long userId) {
        if (player1.getUserId() == userId) return chosenSpell1 != null;
        if (player2 != null && player2.getUserId() == userId) return chosenSpell2 != null;
        return false;
    }

    // -------------------------------------------------------------------------
    // Battle Engine
    // -------------------------------------------------------------------------

    /**
     * Resolve the current round and advance the game state.
     *
     * @return a {@link TurnResult} describing everything that happened
     */
    public TurnResult resolve() {
        int p1HpBefore = player1.getHp();
        int p2HpBefore = player2.getHp();

        TurnResult.Builder rb = TurnResult.builder()
                .turn(turn)
                .p1Name(player1.getName()).p1SpellName(chosenSpell1.getName()).p1SpellEmoji(chosenSpell1.getEmoji())
                .p2Name(player2.getName()).p2SpellName(chosenSpell2.getName()).p2SpellEmoji(chosenSpell2.getEmoji())
                .p1HpBefore(p1HpBefore).p2HpBefore(p2HpBefore);

        // ---- 1. Apply self-effects (Heal) ------------------------------------
        chosenSpell1.apply(player1, player2);
        chosenSpell2.apply(player2, player1);

        int p1Healed = (player1.getHp() - p1HpBefore);     // positive if healed
        int p2Healed = (player2.getHp() - p2HpBefore);
        rb.p1Healed(Math.max(0, p1Healed)).p2Healed(Math.max(0, p2Healed));

        // ---- 2. Compute outgoing damage, modified by target's defensive spell -
        int rawDmgBy1 = chosenSpell1.getRawDamage();  // what p1 tries to deal to p2
        int rawDmgBy2 = chosenSpell2.getRawDamage();  // what p2 tries to deal to p1

        int dmgToP2 = 0, dmgToP1 = 0;
        int reflectedToP1 = 0, reflectedToP2 = 0;

        // p1 attacks p2 — check p2's defense
        if (rawDmgBy1 > 0) {
            switch (chosenSpell2.getEffect()) {
                case SHIELD -> dmgToP2 = rawDmgBy1 / 2;
                case REFLECT -> {
                    dmgToP2 = rawDmgBy1;               // attacker still takes full hit on reflect? No —
                    // design: Reflect absorbs nothing but deflects 50% back
                    dmgToP2 = rawDmgBy1;
                    reflectedToP1 = rawDmgBy1 / 2;
                }
                default -> dmgToP2 = rawDmgBy1;
            }
        }

        // p2 attacks p1 — check p1's defense
        if (rawDmgBy2 > 0) {
            switch (chosenSpell1.getEffect()) {
                case SHIELD -> dmgToP1 = rawDmgBy2 / 2;
                case REFLECT -> {
                    dmgToP1 = rawDmgBy2;
                    reflectedToP2 = rawDmgBy2 / 2;
                }
                default -> dmgToP1 = rawDmgBy2;
            }
        }

        // ---- 3. Apply damage + reflected damage simultaneously ---------------
        player2.takeDamage(dmgToP2);
        player1.takeDamage(dmgToP1);
        player1.takeDamage(reflectedToP1);
        player2.takeDamage(reflectedToP2);

        rb.p1DamageDealt(dmgToP2 + reflectedToP2).p1DamageTaken(dmgToP1 + reflectedToP1).p1ReflectedDamage(reflectedToP1);
        rb.p2DamageDealt(dmgToP1 + reflectedToP1).p2DamageTaken(dmgToP2 + reflectedToP2).p2ReflectedDamage(reflectedToP2);
        rb.p1HpAfter(player1.getHp()).p2HpAfter(player2.getHp());

        // ---- 4. Update cooldowns --------------------------------------------
        advanceCooldowns(cooldowns1, chosenSpell1);
        advanceCooldowns(cooldowns2, chosenSpell2);

        rb.p1FireballCooldown(cooldowns1.getOrDefault("fireball", 0));
        rb.p1ShieldCooldown(cooldowns1.getOrDefault("shield", 0));
        rb.p1ReflectCooldown(cooldowns1.getOrDefault("reflect", 0));
        rb.p2FireballCooldown(cooldowns2.getOrDefault("fireball", 0));
        rb.p2ShieldCooldown(cooldowns2.getOrDefault("shield", 0));
        rb.p2ReflectCooldown(cooldowns2.getOrDefault("reflect", 0));

        // ---- 5. Win check ---------------------------------------------------
        String winner = null;
        if (!player1.isAlive() && !player2.isAlive()) winner = "draw";
        else if (!player2.isAlive()) winner = player1.getName();
        else if (!player1.isAlive()) winner = player2.getName();

        if (winner != null) state = State.FINISHED;
        rb.winner(winner);

        // ---- 6. Prepare next turn -------------------------------------------
        turn++;
        chosenSpell1 = null;
        chosenSpell2 = null;

        return rb.build();
    }

    /**
     * Set the just-cast spell on cooldown and decrement all other running cooldowns.
     */
    private void advanceCooldowns(Map<String, Integer> cds, Spell cast) {
        // Decrement existing cooldowns
        cds.replaceAll((k, v) -> Math.max(0, v - 1));
        // Set new cooldown for the spell just cast
        if (cast.getBaseCooldown() > 0) {
            cds.put(cast.getName().toLowerCase(), cast.getBaseCooldown());
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public long getChatId() { return chatId; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public State getState() { return state; }
    public int getTurn() { return turn; }
    public Map<String, Integer> getCooldowns1() { return cooldowns1; }
    public Map<String, Integer> getCooldowns2() { return cooldowns2; }

    public Spell getChosenSpell1() { return chosenSpell1; }
    public Spell getChosenSpell2() { return chosenSpell2; }

    /**
     * @param userId Telegram user id
     * @return cooldown map belonging to that player, or null if not in this duel
     */
    public Map<String, Integer> getCooldownsFor(long userId) {
        if (player1.getUserId() == userId) return cooldowns1;
        if (player2 != null && player2.getUserId() == userId) return cooldowns2;
        return null;
    }

    public boolean isParticipant(long userId) {
        return player1.getUserId() == userId
                || (player2 != null && player2.getUserId() == userId);
    }
}
