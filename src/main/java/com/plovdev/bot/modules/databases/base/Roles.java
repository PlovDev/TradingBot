package com.plovdev.bot.modules.databases.base;

public enum Roles {
    admin("""
             /addAdmin,/removeAdmin,/changeAdminRole,/mute,/unmute,/ban,
             /unban,/warn,/unwarn,/pendings,/addBadWord,/removeBadWord,
             /pendings,/accept,/reject,/send,/sendTemplate,/sendCustom,
             /templates,/createTemplate,/removeTemplate,/editTemplate
          """),
    moderator("/mute,/unmute,/warn,/unwarn"),

    user("/positions,/history/profile,/referral,/start,/help");

    private final String role;
    Roles(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return this.role.replace(" ", "");
    }
}