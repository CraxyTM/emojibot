package de.felix.emojibot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URL;

public class EmojiBot extends ListenerAdapter {

    public EmojiBot(String discordToken) {
        //Required for downloading images from Discord CDN
        System.setProperty("http.agent", "Chrome");

        JDA jda;
        try {
            jda = JDABuilder.createDefault(discordToken)
                    .setActivity(Activity.playing("/emoji 🤯"))
                    .addEventListeners(this)
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        CommandData emojiCommand = new CommandData("emoji", "Manage the emojis in your server!")
                .addSubcommands(new SubcommandData("add", "Adds a new emoji to your server!")
                        .addOption(OptionType.STRING, "name", "The name of the new emoji", true)
                        .addOption(OptionType.STRING, "link", "The link to the image of the new emoji", true))
                .addSubcommands(new SubcommandData("remove", "Removes an emoji from your server!")
                        .addOption(OptionType.STRING, "emoji", "The emoji to remove", true))
                .addSubcommands(new SubcommandData("help", "Helps you use the EmojiBot"));

        jda.updateCommands().addCommands(emojiCommand).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("Started! wip");
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (!event.getName().equalsIgnoreCase("emoji")) return;

        if ((event.getMember() != null) && !event.getMember().hasPermission(Permission.MANAGE_EMOTES)) {
            event.reply("You must have the " + Permission.MANAGE_EMOTES.getName() + " permission to use this command.")
                    .setEphemeral(false)
                    .queue();
            return;
        }

        if (event.getSubcommandName() == null || event.getGuild() == null) {
            event.reply("Subcommand or guild is null").setEphemeral(true).queue();
            return;
        }

        switch (event.getSubcommandName().toLowerCase()) {
            case "remove": commandRemove(event); break;
            case "add": commandAdd(event); break;
            case "help": commandHelp(event); break;
        }
    }

    private void commandRemove(SlashCommandEvent event) {
        OptionMapping emojiOption = event.getOption("emoji");
        if (emojiOption == null) {
            event.reply("Emoji null!").setEphemeral(true).queue();
            return;
        }

        String emoji = emojiOption.getAsString();

        for (Emote emote : event.getGuild().getEmotes()) {
            if (emoji.contains(emote.getId())) {
                event.reply("Removed Emote: " + emote.getAsMention()).setEphemeral(false).queue();
                emote.delete().reason("Emoji removed by " + event.getUser().getAsTag() + " (" + event.getUser().getId() + ")").complete();
                return;
            }
        }
        event.reply("Cannot find emote").setEphemeral(true).queue();
    }

    private void commandAdd(SlashCommandEvent event) {
        OptionMapping name = event.getOption("name");
        OptionMapping link = event.getOption("link");
        if (name == null || link == null) {
            event.reply("Name or link null!").setEphemeral(true).queue();
            return;
        }

        Icon icon;
        try {
            icon = Icon.from(new URL(link.getAsString()).openStream());
        } catch (IOException e) {
            event.reply("Error while grabbing image from link: " + e.getMessage()).setEphemeral(true).queue();
            return;
        }

        if (icon.getEncoding().getBytes().length > 256000) {
            event.reply("Emoji cannot be larger than 256 kb").setEphemeral(true).queue();
            return;
        }

        try {
            Emote emoji = event.getGuild().createEmote(name.getAsString(), icon).reason("Emoji added by " + event.getUser().getAsTag() + " (" + event.getUser().getId() + ")").complete();
            event.reply("Emoji added " + emoji.getAsMention()).setEphemeral(false).queue();
        } catch (ErrorResponseException e) {
            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void commandHelp(SlashCommandEvent event) {
        event.reply("To be able to use the EmojiBot you need the permission to edit emojis on your server." +
                "\nIf you do not have the permission you might need to ask your server admin to give it to you." +
                "\nIf you have got the permission you can add an emoji via the command: " +
                "\n" +
                "\n/emoji add <name of the emoji> <complete link of the emoji>" +
                "\n" +
                "\nIf you want to delete an emoji you can do it with this command:" +
                "\n" +
                "\n/emoji remove <the emoji you want to remove>" +
                "\n" +
                "\nTo get more information about the EmojiBot you can visit our GitHub-page:" +
                "\n" +
                "\nhttps://github.com/CraxyTM/emojibot ").setEphemeral(true).queue();
    }
}
