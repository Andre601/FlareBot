package com.bwfcwalshy.flarebot.permissions;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class PerGuildPermissions {
    private final HashMap<String, Group> groups = new HashMap<>();
    private final HashMap<String, User> users = new HashMap<>();
    protected String id;

    protected PerGuildPermissions() {
    }

    public PerGuildPermissions(String id) {
        this.id = id;
        if (!hasGroup("Default")) {
            addPermission("Default", "flarebot.skip");
            addPermission("Default", "flarebot.leave");
            addPermission("Default", "flarebot.play");
            addPermission("Default", "flarebot.pause");
            addPermission("Default", "flarebot.info");
            addPermission("Default", "flarebot.join");
            addPermission("Default", "flarebot.skip");
            addPermission("Default", "flarebot.search");
            addPermission("Default", "flarebot.playlist.save");
            addPermission("Default", "flarebot.playlist.load");
            addPermission("Default", "flarebot.help");
        }
    }

    public boolean hasPermission(Member user, String permission) {
        // So we can go into servers and figure out any issues they have.
        if (isCreator(user))
            return true;
        if (user.isOwner())
            return true;
        if (user.getPermissions().contains(Permission.ADMINISTRATOR))
            return true;
        PermissionNode node = new PermissionNode(permission);
        return getUser(user).getGroups().stream()
                .map(this::getGroup)
                .map(Group::getPermissions)
                .flatMap(Collection::stream)
                .map(PermissionNode::new)
                .anyMatch(e -> e.test(node));
    }

    public boolean addPermission(String group, String permission) {
        return getGroup(group).getPermissions().add(permission);
    }

    public boolean removePermission(String group, String permission) {
        boolean had = getGroup(group).getPermissions().remove(permission);
        if (getGroup(group).getPermissions().size() == 0) {
            groups.remove(group);
        }
        return had;
    }

    public User getUser(Member user) {
        return users.computeIfAbsent(user.getUser().getId(), key -> new User(user));
    }

    public Group getGroup(String group) {
        return groups.computeIfAbsent(group, key -> new Group(group));
    }

    public boolean deleteGroup(String group) {
        return groups.remove(group) != null;
    }

    public boolean hasGroup(String group) {
        return groups.containsKey(group);
    }

    public Map<String, Group> getGroups() {
        Map<String, Group> groups = new HashMap<>();
        groups.putAll(this.groups);
        return groups;
    }

    /**
     * Gets the associated guilds ID
     *
     * @return The ID to get
     */
    public String getGuildID() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PerGuildPermissions)) {
            return false;
        }
        PerGuildPermissions otherGuild = (PerGuildPermissions) other;
        return otherGuild.getGuildID().equals(getGuildID());
    }

    public boolean isCreator(Member user) {
        return user.getUser().getId().equals("158310004187725824") || user.getUser().getId().equals("155954930191040513");
    }

    @Override
    public int hashCode() {
        int result = groups.hashCode();
        result = 31 * result + users.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
