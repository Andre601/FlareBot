package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Eval implements Command {
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("nashorn");

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (getPermissions(channel).isCreator(sender)) {
            String code = Arrays.stream(args).collect(Collectors.joining(" "));
            try {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .appendField("Code:", "```js\n" + code + "```", false)
                        .appendField("Result: ", "```js\n" + engine.eval(code) + "```", false).build(), channel);
            } catch (ScriptException e) {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .appendField("Code:", "```js\n" + code + "```", false)
                        .appendField("Result: ", "```js\n" + e.getMessage() + "```", false).build(), channel);
            }
        } else {
            RequestBuffer.request(() -> {
                try {
                    message.addReaction("\u274C");
                } catch (MissingPermissionsException | DiscordException ignored) {
                    ignored.getMessage();
                }
            });
        }
    }

    @Override
    public String getCommand() {
        return "eval";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}
