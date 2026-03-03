package com.wizardduel;

import com.wizardduel.bot.WizardDuelBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Application entry point.
 *
 * <p>Configure your bot via environment variables:
 * <pre>
 *   BOT_USERNAME=YourBotUsername
 *   BOT_TOKEN=123456:ABC-your-token
 * </pre>
 * Or pass them as JVM properties:
 * <pre>
 *   java -DBOT_USERNAME=... -DBOT_TOKEN=... -jar wizard-duel-bot.jar
 * </pre>
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String username = getConfig("username");
        String token    = getConfig("bot_token");
        
        /*String username = getConfig("wizard_duel_bot");
        String token    = getConfig("7978130597:AAFgOH2gnWuIoDGOER1DYam2JOjkO-Q_Tzw");
*/

        if (username == null || token == null) {
            log.error("BOT_USERNAME and BOT_TOKEN must be set as environment variables or system properties.");
            System.exit(1);
        }

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new WizardDuelBot(username, token));
            log.info("🧙 Wizard Duel Bot @{} is running!", username);
        } catch (TelegramApiException e) {
            log.error("Failed to start bot: {}", e.getMessage(), e);
        }
    }

    /** Read from system property first, then environment variable. */
    private static String getConfig(String key) {
        String val = System.getProperty(key);
        if (val == null || val.isBlank()) val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : null;
    }
}
