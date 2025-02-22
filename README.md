<p align="center">
  <img src="https://i.imgur.com/r36XcWN.png" alt="mGiveaway Banner"/>
</p>

<p align="center">
  <a href="https://github.com/m-surowiec/mGiveaway">
    <img src="https://i.imgur.com/nblc47G.png" alt="GitHub Link"/>
  </a>
  <a href="https://discord.gg/MtFgx2jnYE">
    <img src="https://i.imgur.com/1jemm1e.png" alt="Discord Link"/>
  </a>
</p>

<h1 align="center">mGiveaway - Discord Minecraft Giveaway Plugin</h1>

<p align="center">
  <img src="https://i.imgur.com/zI6CWqZ.png" alt="Key Features Icon"/>
</p>

*   **Discord Integration:** Slash commands for easy giveaway creation & management in Discord.
*   **Automated Giveaways:** Schedule start/end times for fully automated giveaways.
*   **Configurable Requirements:** Permissions, Groups/Roles (Vault), PlaceholderAPI values.
*   **In-Game Rewards:** Automatic execution of commands for winners.
*   **Customizable Embeds:** JSON configuration for rich Discord embed messages.
*   **Dynamic Placeholders:** Embeds update with time left, entries, winners, etc.
*   **Giveaway Reminders:** In-game broadcasts to notify players.
*   **Statistics Tracking:** Track entries and wins.
*   **Extensive Text Config:**  `config.yml` for messages, UI text, etc.
*   **Update Checker:** In-game notifications for new versions.


<p align="center">
  <img src="https://i.imgur.com/feLp8Uj.png" alt="Requirements Icon"/>
</p>

*   **Paper Server:** 1.17+
*   **PlaceholderAPI:** 2.11.6+
*   **Vault:** 1.7+


<p align="center">
  <img src="https://i.imgur.com/zQFONmG.png" alt="Setup & Installation Icon"/>
</p>

1.  **Download & Install:** Get `mGiveaway-VERSION.jar` from [GitHub Releases](https://github.com/m-surowiec/mGiveaway/releases/latest) and place in `/plugins` folder.
2.  **Restart your server**
3.  **Configure Discord Bot in `config.yml`:**
    *   Create Bot at [Discord Developer Portal](https://discord.com/developers/applications).
    *   Get Bot Token ("Bot" tab).
    *   Paste Token into `discord.bot.token` in `plugins/mGiveaway/config.yml`
4.  **Configure `config.yml` for:**
    *   `prefix`: In-game prefix.
    *   `broadcast_interval` & `broadcast_message`: Giveaway reminders.
    *   `discord.bot` settings (token, status, activity, channels, etc.) - **Token & giveaway_channel REQUIRED**.
    *   `discord.bot.command` customization.
    *   `discord.bot.giveaway_embed`, `giveaway_end_embed`, `log_embed`: JSON Embed customization (use [Discord Embed Builder](https://glitchii.github.io/embedbuilder/)).
    *   `messages`: All text messages.
    *   `giveaways`: Define giveaway settings & requirements.
5.  **Reload Plugin:** Use `/mgwreload` or restart server.

<p align="center">
  <img src="https://i.imgur.com/mW79SAM.png" alt="Slash Command Icon"/>
</p>

Use `/create_giveaway` (default) to create giveaways. 
```/create_giveaway name:<giveaway_name> prize:<prize_description> minecraft_prize:<minecraft_prize_placeholder> duration:<duration_string> winners:<number_of_winners> command:<reward_command> requirements:<true/false>```

*   **name:** **REQUIRED:** Internal giveaway name.
*   **prize:** **REQUIRED:** Formatted prize description (Discord).
*   **minecraft_prize:** **REQUIRED:** Plain-text prize (in-game broadcast).
*   **duration:** **REQUIRED:** Duration string (e.g., `2d 30m`).
*   **winners:** **REQUIRED:** Number of winners.
*   **command:** **REQUIRED:** First reward command (`%player%` placeholder).
*   **requirements:** **OPTIONAL:** `true`/`false` (if the giveaway start should wait for requirements)


### ⚙️ Configuring Giveaways (`config.yml` - `giveaways` section):

See the example giveaway configuration (`giveaways.5Diamonds`) in the `config.yml` for a template. Key configuration options for each giveaway defined under the `giveaways` section:

*   **settings:** - Settings for the giveaway
    *   `scheduled_start`: (Optional) Date and time to automatically start the giveaway at a future time. Format: `dd/MM/yyyy HH:mm:ss` (24-hour format). If not set, the giveaway starts immediately (or when manually forced).
    *   `end_time`: **REQUIRED:** Date and time to automatically end the giveaway. Format: `dd/MM/yyyy HH:mm:ss` (24-hour format).
    *   `winners`: **REQUIRED:** The number of winners for this giveaway.
    *   `commands`: **REQUIRED:** Reward commands (`%player%`).
    *   `prize_formatted`: **REQUIRED:** Discord prize description.
    *   `minecraft_prize`: **REQUIRED:** In-game prize text.
*   `requirements:` (Optional)
    *   `group`: Vault Group/Role.
        *   `<group_name>`: `value: true/false`, `formatted`.
    *   `permission`: Vault Permission.
        *   `<permission_node>`: `value: true/false`, `formatted`.
    *   `placeholder`: PlaceholderAPI value.
        *   `<placeholder_name>`: `over: <number>` or `under: <number>`, `formatted`.

<p align="center">
  <img src="https://i.imgur.com/ncy99or.png" alt="Placeholders Icon"/>
</p>

#### For Giveaway and Giveaway End Embeds:
*   `{TIME-LEFT}`, `{END-TIME}`, `{ENTRIES}`, `{WIN-COUNT}`,
    `{PRIZE}`, `{WINNERS}`

#### For Log Embed:
*   `{GIVEAWAY-NAME}`, `{ENTRIES-COUNT}`, `{PRIZE}`, `{COMMANDS}`,
    `{WINNERS-MENTIONS}`, `{ENTRIES-LIST}`


<p align="center">
  <img src="https://i.imgur.com/Yum0oka.png" alt="Important Notes Icon"/>
</p>

*   **Paper server is required. This plugin won't work on Spigot**
*   After making any changes to the `config.yml` file, remember to use `/mgwreload` in-game to reload the plugin.
    Sometimes, the plugin pauses itself because of runtime errors. To restart it just reload it ;)


[![bStats: Paper Servers](https://bstats.org/signatures/bukkit/mGiveaway.svg)](https://bstats.org/plugin/bukkit/mGiveaway/24362)
