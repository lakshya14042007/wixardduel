# 🧙 Wizard Duel – Telegram Bot (Java / Maven)

A 2-player turn-based wizard duel game for Telegram group chats.

---

## Features

| Spell | Effect | Cooldown |
|-------|--------|----------|
| ⚡ Attack | 20 damage | None |
| 🔥 Fireball | 30 damage | 2 turns |
| 🛡️ Shield | Blocks 50 % of incoming damage | 1 turn |
| 🔮 Reflect | Reflects 50 % of incoming damage | 1 turn |
| 💚 Heal | Restores 20 HP | None |

- Simultaneous spell resolution (both players choose secretly each round)
- Per-player cooldown tracking
- Visual HP bars in chat
- Supports multiple concurrent duels across different group chats

---

## Prerequisites

- Java 17+
- Maven 3.8+

---

## Build

```bash
mvn clean package -q
```

This produces `target/wizard-duel-bot-1.0.0.jar` (fat JAR, all dependencies bundled).

---

## Configuration

The bot reads two values — set them as **environment variables** or **JVM system properties**:

| Key | Description |
|-----|-------------|
| `BOT_USERNAME` | Bot username without `@`, e.g. `WizardDuelBot` |
| `BOT_TOKEN` | Token from [@BotFather](https://t.me/BotFather) |

### Create your bot

1. Open Telegram, message **@BotFather**
2. Send `/newbot` and follow the prompts
3. Copy the token you receive
4. Disable privacy mode: `/setprivacy` → choose your bot → `Disable`  
   *(required so the bot can see all group messages)*

---

## Run

### Using environment variables (recommended)

```bash
export BOT_USERNAME=YourBotUsername
export BOT_TOKEN=123456:ABC-your-token-here
java -jar target/wizard-duel-bot-1.0.0.jar
```

### Using JVM properties

```bash
java -DBOT_USERNAME=YourBotUsername \
     -DBOT_TOKEN=123456:ABC-your-token-here \
     -jar target/wizard-duel-bot-1.0.0.jar
```

---

## Commands

| Command | Description |
|---------|-------------|
| `/startduel` | Challenge the group to a wizard duel |
| `/joinduel` | Accept an open challenge |
| `/spell <name>` | Cast a spell this round |
| `/status` | Show HP bars and cooldowns |
| `/spells` | List all spells |
| `/cancelduel` | Cancel the current duel (challenger only) |
| `/help` | Show help |

---

## Example play-through

```
Alice: /startduel
Bot:   🧙 Wizard Duel! Alice wants to duel!

Bob:   /joinduel
Bot:   ⚔️ Duel started: Alice vs Bob! …Round 1 – cast your spells.

Alice: /spell fireball
Bob:   /spell shield
Bot:   💥 Resolving Round 1… Alice dealt 15 dmg to Bob (Fireball vs Shield)
       ❤️ Alice: ██████████ 100/100
       ❤️ Bob:   ████████░░ 85/100
```

---

## Project Structure

```
src/main/java/com/wizardduel/
├── Main.java                   – Entry point
├── model/
│   └── Player.java             – Wizard state (HP, name)
├── spell/
│   ├── Spell.java              – Abstract base class
│   ├── SpellEffect.java        – SHIELD / REFLECT enum
│   ├── Spells.java             – All 5 concrete spell classes
│   └── SpellRegistry.java      – Factory / lookup
├── game/
│   ├── GameSession.java        – Game state + battle engine
│   ├── TurnResult.java         – Immutable round outcome DTO
│   └── MessageFormatter.java   – Telegram message builder
└── bot/
    └── WizardDuelBot.java      – Telegram command handler
```
