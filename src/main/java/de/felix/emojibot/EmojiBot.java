package de.felix.emojibot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;

public class EmojiBot {

    public EmojiBot(String discordToken) {
        DiscordApi discord = new DiscordApiBuilder().setToken(discordToken).login().join();
        discord.updateActivity(ActivityType.PLAYING, "with Emojis 🤯");


    }

}
