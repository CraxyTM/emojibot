package de.felix.emojibot;

import io.javalin.Javalin;

public class Main {

    private static final String DISCORD_TOKEN = "DISCORD_BOT_TOKEN";

    public static void main(String[] args) {
        String discordToken = System.getenv(DISCORD_TOKEN);

        if (discordToken == null || discordToken.isEmpty()) {
            System.err.println("Please provide the environmental variable: " + DISCORD_TOKEN);
            System.exit(-1);
            return;
        }

        new EmojiBot(discordToken);

        Javalin.create().get("/", (ctx) -> ctx.result("ok")).start(80);
    }

}
