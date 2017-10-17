package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PurgeCommand implements Command {

    private Map<String, Long> cooldowns = new HashMap<>();
    private static final long cooldown = 60000;

    // Commented out until I know the new one compiles and works.
    /*@Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && args[0].matches("\\d+")) {
            if (!FlareBotManager.getInstance().getGuild(channel.getId()).getPermissions().isCreator(sender)) {
                long calmitdood = cooldowns.computeIfAbsent(channel.getGuild().getId(), n -> 0L);
                if (System.currentTimeMillis() - calmitdood < cooldown) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String
                                    .format("You are on a cooldown! %s seconds left!",
                                            (cooldown - (System
                                                    .currentTimeMillis() - calmitdood)) / 1000))
                            .build()).queue();
                    return;
                }
            }
            int count;
            try {
                count = Integer.parseInt(args[0]) + 1;
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage("The number entered is too high!", channel);
                return;
            }
            if (count < 2) {
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("Can't purge less than 2 messages!").build()).queue();
            }
            List<Permission> perms = channel.getGuild().getSelfMember().getPermissions(channel);
            if (perms.contains(Permission.MESSAGE_HISTORY) && perms.contains(Permission.MESSAGE_MANAGE)) {
                try {
                    MessageHistory history = new MessageHistory(channel);
                    int toRetrieve = count;
                    while (history.getRetrievedHistory().size() < count) {
                        if (history.retrievePast(Math.min(toRetrieve, 100)).complete().isEmpty())
                            break;
                        toRetrieve -= Math.min(toRetrieve, 100);
                        if (toRetrieve < 2)
                            toRetrieve = 2;
                    }
                    int i = 0;
                    List<Message> toDelete = new ArrayList<>();
                    for (Message m : history.getRetrievedHistory()) {
                        if (m.getCreationTime().plusWeeks(2).isAfter(OffsetDateTime.now())) {
                            i++;
                            toDelete.add(m);
                        }
                        if (toDelete.size() == 100) {
                            channel.deleteMessages(toDelete).complete();
                            toDelete.clear();
                        }
                    }
                    if (!toDelete.isEmpty()) {
                        if (toDelete.size() != 1)
                            channel.deleteMessages(toDelete).complete();
                        else toDelete.forEach(mssage -> mssage.delete().complete());
                    }
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String.format("Deleted `%s` messages!", i-1)).build())
                            .queue(s -> new FlareBotTask("Delete Message " + s) {
                                @Override
                                public void run() {
                                    s.delete().queue();
                                }
                            }.delay(TimeUnit.SECONDS.toMillis(5)));
                } catch (Exception e) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String
                                    .format("Failed to bulk delete or load messages! Error: `%s`", e))
                            .build()).queue();
                }
            } else {
                channel.sendMessage("Insufficient permissions! I need `Manage Messages` and `Read Message History`")
                        .queue();
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender);
        }
    }*/

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length >= 2) {
            // clean all 5
            User targetUser = null;
            if(!args[0].equalsIgnoreCase("all")) {
                targetUser = GeneralUtils.getUser(args[0], guild.getGuildId(), true);
                if(targetUser == null) {
                    MessageUtils.sendErrorMessage("That target user cannot be found, try mentioning them, using the user ID or using `all` to clear the entire chat.", channel);
                    return;
                }                
            }

            int amount = GeneralUtils.getInt(args[1], -1);

            // 2 messages min
            if(amount < 1) {
                MessageUtils.sendErrorMessage("You must purge at least 1 message, please give a vaid purge amount.", channel);
                return;
            }

            // This will be a successful delete so limit here.
            if (!guild.getPermissions().isCreator(sender)) {
                long riotPolice = cooldowns.computeIfAbsent(channel.getGuild().getId(), n -> 0L);
                if (System.currentTimeMillis() - riotPolice < cooldown) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String
                                    .format("You are on a cooldown! %s seconds left!",
                                            (cooldown - (System
                                                    .currentTimeMillis() - riotPolice)) / 1000))
                            .build()).queue();
                    return;
                }
            }

            if(!guild.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
                MessageUtils.sendErrorMessage("I do not have the perms `Manage Messages` and `Read Message History` please make sure I have these and do the command again.", channel);
                return;
            }
            MessageHistory history = new MessageHistory(channel);
            int toRetrieve = amount + 1;
            int i = 0;
            outer:
            while(toRetrieve > 0) { // I don't really know if this should be min... 
                                    // since deleting 10 of someone could be like 100 back yet this would request it 10 times. 
                                    // For now I will just request 100 here each time.
                if(history.retrievePast(100).complete().isEmpty()) {
                    break;
                }

                List<Message> toDelete = new ArrayList<>();
                for(Message msg : history.getRetrievedHistory()) {
                    if(msg.getCreationTime().plusWeeks(2).isBefore(OffsetDateTime.now())) break outer;
                    if(targetUser != null && msg.getAuthor().getId().equals(targetUser.getId()))
                        toDelete.add(msg);
                    else
                        toDelete.add(msg);
                    i++;
                }
                channel.deleteMessages(toDelete).complete();
                toRetrieve -= toDelete.size();
                toDelete.clear();
            }
            channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String.format("Deleted `%s` messages!", i-1)).build())
                            .queue(s -> new FlareBotTask("Delete Message " + s) {
                                @Override
                                public void run() {
                                    s.delete().queue();
                                }
                            }.delay(TimeUnit.SECONDS.toMillis(5)));
        }
    }

    @Override
    public String getCommand() {
        return "purge";
    }

    @Override
    public String getDescription() {
        return "Removes last X messages.";
    }

    @Override
    public String getUsage() {
        return "`{%}purge <messages>` - Purges a certain amount of messages";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"clean"};
    }

    @Override
    public boolean deleteMessage() {
        return false;
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.MESSAGE_MANAGE);
    }
}
