# mGiveaway - Minecraft Discord Giveaway Plugin

[![bStats: Paper Servers](https://bstats.org/signatures/bukkit/mGiveaway.svg)](https://bstats.org/plugin/bukkit/mGiveaway/24362)

**mGiveaway** is a Minecraft plugin for Paper servers that allows you to create and manage giveaways directly from Discord, rewarding your players with in-game prizes.  It uses Discord slash commands for easy giveaway creation and button/modal interactions for user-friendly entry.

**Key Features:**

* **Discord Integration:** Create and manage giveaways directly from Discord using slash commands.
* **Automated Giveaways:** Schedule giveaways to start and end at specific times.
* **Configurable Requirements:** Set requirements for giveaway entry based on:
    * Permissions (Vault)
    * Groups/Roles (Vault)
    * PlaceholderAPI values (numerical comparisons)
* **Discord Entry System:** Players enter giveaways by clicking a button in Discord and submitting their Minecraft username via a modal.
* **Automatic Winner Selection:** Plugin automatically selects winners when the giveaway ends.
* **In-Game Rewards:** Execute configurable commands to reward winners automatically in-game.
* **Discord Embed Customization:** Fully customize giveaway embed messages in Discord using a JSON configuration, with placeholders for time left, entries, prize, winners, and more.
* **Giveaway Reminders:** Broadcast configurable in-game messages to remind players about active giveaways.
* **Statistics Tracking:** Track player giveaway entries and wins.
* **SQLite Database:** Stores giveaway entries in a local SQLite database.
* **PlaceholderAPI and Vault Support:** Extensively utilizes PlaceholderAPI for flexible requirements and Vault for permission/group checks.

**Platform:**
* **Paper**

**Dependencies:**

* **Paper API 1.20+**
* **PlaceholderAPI** (Required)
* **Vault** (Soft-dependency, highly recommended for permission and group requirements)
* **JDA (Java Discord API)** (Included with the plugin)

**Setup & Installation:**

1. **Download:** Download the latest `mGiveaway.jar` file from [https://github.com/m-surowiec/mGiveaway/releases/latest].
2. **Install:** Place the `mGiveaway.jar` file into the `plugins` folder of your Paper server.
3. **Restart Server:** Restart your Minecraft server.
4. **Configure Discord Bot:**
    * **Create a Discord Bot:** If you haven't already, create a Discord bot application at [Discord Developer Portal](https://discord.com/developers/applications).
    * **Get Bot Token:** Obtain the bot token from your bot application (Bot tab).
    * **Invite Bot to Server:** Invite your bot to your Discord server using the OAuth2 URL Generator (OAuth2 -> URL Generator, select `bot` scope and `applications.commands` permission, and copy the generated URL).
5. **Configure Plugin (`config.yml`):**
    * **Locate `config.yml`:** After the first server startup, a `mGiveaway` folder will be created in your `plugins` directory. Inside, you'll find `config.yml`.
    * **Edit `config.yml`:** Open `config.yml` and configure the following:
        * `prefix`:  Customize the in-game message prefix.
        * `broadcast_interval`: Set the interval (in seconds) for giveaway reminder broadcasts in-game.
        * `broadcast_message`: Customize the in-game reminder message (uses placeholders).
        * `discord.bot.token`: **REQUIRED:** Enter your Discord bot token here.
        * `discord.bot.status`:  Set the bot's status (e.g., `ONLINE`, `IDLE`, `DND`).
        * `discord.bot.activity`: Set the bot's activity type (e.g., `PLAYING`, `WATCHING`, `LISTENING`, `STREAMING`).
        * `discord.bot.activity_text`: Set the text for the bot's activity.
        * `discord.bot.activity_url`: Set the URL if using `STREAMING` activity.
        * `discord.bot.giveaway_channel`: **REQUIRED:**  Enter the ID of the Discord text channel where giveaway embeds will be posted.
        * `discord.bot.log_embed_channel`: **REQUIRED:** Enter the ID of the Discord text channel for giveaway end logs.
        * `discord.bot.log_embed_color`: Customize the color of the giveaway log embed.
        * `discord.bot.command.name`: Customize the Discord slash command name (default: `oneblock_create_giveaway`).
        * `discord.bot.command.description`: Customize the Discord slash command description.
        * `discord.bot.giveaway_embed`: **REQUIRED:** Customize the JSON structure of the giveaway embed message.  See example in `config.yml` and placeholders below.
        * `discord.bot.giveaway_end_embed`: **REQUIRED:** Customize the JSON structure of the giveaway end embed message. See example and placeholders.
        * `giveaways`: Configure your giveaways under this section. See example giveaway (`10KluczyPodniebnych`) in `config.yml` for details on setting up giveaway settings and requirements.

6. **Reload Plugin (Optional):** You can use a plugin manager to reload the `mGiveaway` plugin after configuring `config.yml` without restarting the entire server (e.g., `/reload confirm` on Paper, or plugin-specific reload commands if available).

**Slash Command Usage (Discord):**

Use the configured slash command (default: `/oneblock_create_giveaway`) in your Discord server to create a giveaway.

* `/oneblock_create_giveaway name:<giveaway_name> prize:<prize_description> prize_placeholder:<placeholder_text> duration:<duration_string> winners:<number_of_winners> command:<reward_command> requirements:<true/false>`

   * `name`:  The name of the giveaway (used internally, e.g., in config).
   * `prize`:  The formatted description of the prize to display in the Discord embed.
   * `prize_placeholder`: A short placeholder for the prize used in in-game broadcast messages.
   * `duration`: The giveaway duration (e.g., `1mo 2w 7d 5h 30m`). Units: `mo` (months), `w` (weeks), `d` (days), `h` (hours), `m` (minutes), `s` (seconds).
   * `winners`: The number of winners for the giveaway.
   * `command`: The first command to execute for each winner (use config for multiple commands). Placeholders: `%player%` (winner's Minecraft username).
   * `requirements`: (Optional, `true` or `false`, defaults to `false`). Set to `true` if you want to manually add requirements to the giveaway in the `config.yml` *before* it starts. If `false`, the giveaway starts immediately after creation.

**Configuring Giveaways (`config.yml` - `giveaways` section):**

See the example giveaway configuration (`giveaways.10KluczyPodniebnych`) in the `config.yml` for a template.  Key configuration options for each giveaway:

* `settings.scheduled_start`: (Optional) Date and time to automatically start the giveaway (format: `dd/MM/yyyy HH:mm:ss`). If not set, the giveaway starts immediately (or when forced).
* `settings.end_time`: **REQUIRED:** Date and time to automatically end the giveaway (format: `dd/MM/yyyy HH:mm:ss`).
* `settings.winners`: **REQUIRED:** Number of winners.
* `settings.commands`: **REQUIRED:** List of commands to execute for each winner. Placeholders: `%player%`.
* `settings.prize_formatted`: **REQUIRED:** Formatted prize description for Discord embed.
* `settings.prize_placeholder`: **REQUIRED:** Short placeholder for prize in in-game broadcasts.
* `requirements`: (Optional) Define requirements for giveaway entry:
    * `group`:  Role/group based requirements (using Vault).
    * `permission`: Permission-based requirements (using Vault).
    * `placeholder`: PlaceholderAPI value-based requirements (numerical comparisons).
    * See `config.yml` example for detailed syntax and options for defining requirements, including `value`, `formatted` (for display), `over`/`under` (for number placeholders).

**Embed JSON Placeholders (`discord.bot.giveaway_embed`, `discord.bot.giveaway_end_embed`):**

Use these placeholders in your embed JSON configurations to dynamically display giveaway information:

* `{TIME-LEFT}`: Time remaining until giveaway ends (Discord relative timestamp).
* `{END-TIME}`: End time of the giveaway (formatted date and time).
* `{ENTRIES}`: Number of current giveaway entries.
* `{WIN-COUNT}`: Number of winners.
* `{PRIZE}`: Formatted prize description.
* `{WINNERS}`:  (Giveaway end embed only) List of winners (Discord mentions and usernames).
