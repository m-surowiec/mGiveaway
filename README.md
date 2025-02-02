# mGiveaway - Minecraft Discord Giveaway Plugin

[![bStats: Paper Servers](https://bstats.org/signatures/bukkit/mGiveaway.svg)](https://bstats.org/plugin/bukkit/mGiveaway/24362)

**mGiveaway** is a powerful plugin that allows you to seamlessly create and manage engaging giveaways directly from Discord, rewarding winners automatically in your Minecraft server. Utilizing Discord slash commands for easy giveaway creation and interactive button/modal interfaces for user-friendly entry, mGiveaway simplifies the process of running giveaways and boosting player engagement.

**✨ Key Features:**

*   **Discord Integration:** Create, manage, and start giveaways directly from Discord using intuitive slash commands.
*   **Automated Giveaways:** Schedule giveaways to start and end at specific times, running completely automatically.
*   **Configurable Entry Requirements:** Define flexible entry requirements based on:
    *   **Permissions:** (Using Vault) Require players to have specific permissions.
    *   **Groups/Roles:** (Using Vault) Limit entry to players in certain groups or roles.
    *   **PlaceholderAPI Values:** Set numerical requirements based on PlaceholderAPI values (e.g., minimum playtime, balance, etc.).
*   **Automated In-Game Rewards:** Automatically execute configurable commands to reward giveaway winners directly in-game upon giveaway completion.
*   **Highly Customizable Discord Embeds:** Fully customize the appearance of giveaway messages in Discord using JSON configurations. Embed customization includes:
    *   Titles, descriptions, colors, timestamps
    *   Footers, thumbnails, images, authors
    *   Multiple fields with dynamic placeholders
*   **Dynamic Embed Placeholders:** Embed messages dynamically display giveaway information using placeholders for:
    *   Time left, end time, number of entries, winner count, prize, and winner lists.
*   **Configurable Giveaway Reminders:** Broadcast customizable in-game messages to remind players about active giveaways at set intervals.
*   **Statistics Tracking:** Track player giveaway entries and wins for engagement analysis.
*   **Extensive Text Configurability:** Nearly all user-facing text within the plugin (in-game messages, Discord messages, UI elements) is now configurable and translatable via `config.yml`.
*   **Automatic Update Checker:** Notifies server operators in-game about new mGiveaway versions, ensuring you always have the latest features and fixes.

**✅ Requirements:**

*   **Paper Server:** 1.17+ (Designed and tested for Paper servers for optimal performance and compatibility)
*   **PlaceholderAPI:** 2.11.6+
*   **Vault:** 1.7+

**⚙️ Setup & Installation:**

1.  **Download:** Download the latest `mGiveaway-VERSION.jar` file from the [GitHub Releases page](https://github.com/m-surowiec/mGiveaway/releases/latest).
2.  **Install:** Place the downloaded `.jar` file into the `plugins` folder of your Paper server.
3.  **Restart Server:** Restart your Minecraft server to load the plugin.
4.  **Configure Discord Bot:**
    *   **Create a Discord Bot:** Create a Discord Bot application at the [Discord Developer Portal](https://discord.com/developers/applications).
    *   **Get Bot Token:** Obtain the Bot Token from your bot application's "Bot" tab.
    *   **Enter Token in `config.yml`:** Paste the Bot Token into the `discord.bot.token` field in the `config.yml` file located in the `plugins/mGiveaway/` folder.
5.  **Configure Plugin (`config.yml`):**
    *   **Edit `config.yml`:** Open the `config.yml` file in the `plugins/mGiveaway/` folder and configure the following settings to your preferences:
        *   `prefix`: Customize the in-game message prefix.
        *   `broadcast_interval`: Set the interval (in seconds) for giveaway reminder broadcasts in-game.
        *   `broadcast_message`: Customize the in-game giveaway reminder message (supports placeholders).
        *   `discord.bot.token`: **REQUIRED:** Enter your Discord Bot Token here.
        *   `discord.bot.status`: Set the Discord Bot status (e.g., `ONLINE`, `IDLE`, `DND`, `OFFLINE`).
        *   `discord.bot.activity`: Set the Discord Bot activity type (e.g., `PLAYING`, `WATCHING`, `LISTENING`, `STREAMING`).
        *   `discord.bot.activity_text`: Set the text for the Discord Bot activity (e.g., "Minecraft Giveaways").
        *   `discord.bot.activity_url`: Set the URL for `STREAMING` activity type (if applicable).
        *   `discord.bot.giveaway_channel`: **REQUIRED:** Enter the ID of the Discord text channel where giveaway embeds will be posted.
        *   `discord.bot.log_embed_channel`: Enter the ID of the Discord text channel for giveaway end logs (optional, but recommended).
        *   `discord.bot.log_embed_color`: Customize the color of the giveaway log embed messages in Discord (Hex color code).
        *   `discord.bot.command.name`: Customize the Discord slash command name (default: `create_giveaway`).
        *   `discord.bot.command.description`: Customize the Discord slash command description.
        *   `discord.bot.command.options`: Customize descriptions for each slash command option for localization and clarity.
        *   `discord.bot.giveaway_embed`: Customize the JSON structure for the main giveaway embed message. See example in `config.yml` and placeholders below. (Use online tools like [Discord Embed Builder](https://glitchii.github.io/embedbuilder/) for easier embed creation).
        *   `discord.bot.giveaway_end_embed`: Customize the JSON structure for the giveaway end embed message. See example and placeholders in `config.yml`.
        *   `discord.bot.log_embed`: Customize the JSON structure for the giveaway log embed message. See example and placeholders in `config.yml`.
        *   `messages`: Customize all user-facing text messages for in-game and Discord interactions, including error messages, success messages, button text, modal titles, and more.
        *   `giveaways`: Configure your giveaways under this section. See the example giveaway (`5Diamonds`) in `config.yml` for details on setting up individual giveaway settings and requirements.

6.  **Reload Plugin:**
    *   Use a plugin manager plugin like [PlugManX](https://www.spigotmc.org/resources/plugmanx.88135/) and run `/plugman reload mGiveaway` for a soft reload.
    *   Alternatively, restart your Paper server for a full reload.

**⌨️ Slash Command Usage (Discord):**

Use the configured slash command (default: `/create_giveaway`) in your Discord server to create a new giveaway.

*   `/create_giveaway name:<giveaway_name> prize:<prize_description> minecraft_prize:<minecraft_prize_placeholder> duration:<duration_string> winners:<number_of_winners> command:<reward_command> requirements:<true/false>`

    *   `name`: **REQUIRED:** The internal name of the giveaway (used for configuration and identification within the plugin, not displayed to users).
    *   `prize`: **REQUIRED:** The formatted description of the prize as it will appear in the Discord embed message (supports rich text formatting).
    *   `minecraft_prize`: **REQUIRED:** A short, plain-text placeholder for the prize, used in in-game broadcast messages (keep it concise).
    *   `duration`: **REQUIRED:** The duration of the giveaway, specified as a string (e.g., `1mo 2w 7d 5h 30m`). Supported time units: `mo` (months), `w` (weeks), `d` (days), `h` (hours), `m` (minutes), `s` (seconds).
    *   `winners`: **REQUIRED:** The number of winners to be selected for the giveaway (integer).
    *   `command`: **REQUIRED:** The first command to be executed for each winner when they claim their prize. Use `%player%` as a placeholder for the winner's Minecraft username. You can configure additional commands directly in the `config.yml` for each giveaway.
    *   `requirements`: **OPTIONAL:** (`true` or `false`, defaults to `false`). Set to `true` if you intend to manually define entry requirements for this giveaway in the `config.yml` *before* starting it. If set to `false` or omitted, the giveaway will start immediately upon creation (without requirements).

**⚙️ Configuring Giveaways (`config.yml` - `giveaways` section):**

See the example giveaway configuration (`giveaways.5Diamonds`) in the `config.yml` for a detailed template. Key configuration options for each giveaway defined under the `giveaways` section:

*   `settings:` - Settings for the giveaway
    *   `settings.scheduled_start`: (Optional) Date and time to automatically start the giveaway at a future time. Format: `dd/MM/yyyy HH:mm:ss` (24-hour format). If not set, the giveaway starts immediately (or when manually forced).
    *   `settings.end_time`: **REQUIRED:** Date and time to automatically end the giveaway. Format: `dd/MM/yyyy HH:mm:ss` (24-hour format).
    *   `settings.winners`: **REQUIRED:** The number of winners for this giveaway.
    *   `settings.commands`: **REQUIRED:** A list of server commands to execute for each winner when they win. Use `%player%` as a placeholder for the winner's Minecraft username in commands.
    *   `settings.prize_formatted`: **REQUIRED:** The formatted prize description that will be displayed in the Discord giveaway embed message.
    *   `settings.minecraft_prize`: **REQUIRED:** A short, plain-text prize placeholder used in in-game broadcast messages.
*   `requirements:` - (Optional) Define entry requirements for the giveaway. If omitted, no requirements are enforced.
    *   `requirements.group:` - Role/Group-based requirements (using Vault).
        *   `<group_name>:` - Name of the Vault-managed group/role.
            *   `value: true/false` - Set to `true` to require the player to be in this group, `false` to require them *not* to be in this group.
            *   `formatted:` (Optional) A user-friendly formatted string to describe this requirement in error messages (e.g., `"VIP rank"`).
    *   `requirements.permission:` - Permission-based requirements (using Vault).
        *   `<permission_node>:` -  Permission node to check. Use `-` instead of `.` in the node name in `config.yml` (e.g., `mcgramy-giveaway` for `mcgramy.giveaway`).
            *   `value: true/false` - Set to `true` to require the player to have this permission, `false` to require they *not* have this permission.
            *   `formatted:` (Optional) A user-friendly formatted string to describe this requirement (e.g., `"Giveaway Permission"`).
    *   `requirements.placeholder:` - PlaceholderAPI value-based requirements (numerical comparisons).
        *   `<placeholder_name>:` - Name of the PlaceholderAPI placeholder (without `%` or `%%`).
            *   `over: <number>` - (Optional) Require the placeholder value to be *greater than* this number.
            *   `under: <number>` - (Optional) Require the placeholder value to be *less than or equal to* this number.
            *   `formatted:` (Optional) A user-friendly formatted string to describe this requirement (e.g., `"Playtime Requirement"`).

**Discord Embed JSON Placeholders (`discord.bot.giveaway_embed`, `discord.bot.giveaway_end_embed`, `discord.bot.log_embed`):**

Use these placeholders within your embed JSON configurations to dynamically display giveaway information:

*   **For Giveaway and Giveaway End Embeds:**
    *   `{TIME-LEFT}`: Time remaining until the giveaway ends (Discord relative timestamp format).
    *   `{END-TIME}`: The exact end time of the giveaway (formatted date and time).
    *   `{ENTRIES}`: The current number of entries in the giveaway.
    *   `{WIN-COUNT}`: The number of winners for the giveaway.
    *   `{PRIZE}`: The formatted prize description from the giveaway settings.
    *   `{WINNERS}`: (For giveaway end embeds only) A list of giveaway winners, mentioning them in Discord format and displaying their submitted usernames.

*   **For Log Embeds (`discord.bot.log_embed`):**
    *   `{GIVEAWAY-NAME}`: The name of the giveaway.
    *   `{ENTRIES-COUNT}`: The total number of entries in the giveaway.
    *   `{PRIZE}`: The prize description from the giveaway settings.
    *   `{COMMANDS}`: A comma-separated list of reward commands configured for the giveaway.
    *   `{WINNERS-MENTIONS}`: Mentions of all winners in Discord format.
    *   `{ENTRIES-LIST}`: A list of all entrants, mentioning them and displaying their submitted usernames.

**Important Notes:**

*   **Paper Server Required:** mGiveaway is designed and optimized for Paper servers. While it *may* load on Spigot or Bukkit, full functionality and performance are not guaranteed outside of the Paper server environment.
*   **Configuration Reload:** After making any changes to the `config.yml` file, remember to use the `/mgwreload` command in-game to reload the plugin and apply the new settings without requiring a full server restart.

**ToDo:**
* **API**
* **Auto giveaway deletion from config and upload to database**
* **Stats impemenation**
* **Fix update checker**
