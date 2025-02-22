<p align="center">
  <img src="https://media.discordapp.net/attachments/1340978735326953495/1341122245392597052/banerkolka.png?ex=67b4d8ec&is=67b3876c&hm=28e641bfa7acbeb11b7fc6ed1728ef902522d1c3052f2b2a67d417f9ec0430dc&=&format=webp&quality=lossless" alt="mGiveaway Banner"/>
</p>

<p align="center">
  <a href="https://github.com/m-surowiec/mGiveaway">
    <img src="https://media.discordapp.net/attachments/1340978735326953495/1341127089289433118/2.png?ex=67b4dd6f&is=67b38bef&hm=fc66979c45e40a8a85733405921a22768f64c43119f8059a21e9ce59283d71b7&=&format=webp&quality=lossless" alt="GitHub Link"/>
  </a>
  <a href="https://discord.gg/MtFgx2jnYE">
    <img src="https://media.discordapp.net/attachments/1340978735326953495/1341127088979185734/1.png?ex=67b4dd6e&is=67b38bee&hm=93ce2e7acabb55cc7385ac377da168ae2d6af3dafb2673e52965086ed14452a8&=&format=webp&quality=lossless" alt="Discord Link"/>
  </a>
</p>

<h1 align="center">mGiveaway - Discord Minecraft Giveaway Plugin</h1>

<p align="center">
  <img src="https://media.discordapp.net/attachments/1339307861536276663/1341123337107275848/key.png?ex=67b4d9f0&is=67b38870&hm=6c8745f955060957a6cae0b0a235793cae4464fa83481b9baac479dfb2f1ba98&=&format=webp&quality=lossless" alt="Key Features Icon"/>
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
  <img src="https://media.discordapp.net/attachments/1339307861536276663/1341123337593552987/req.png?ex=67b4d9f0&is=67b38870&hm=a34a5ca0e9344b7e283083eec5754dbdcce0d75da5a922356b900558f384463a&=&format=webp&quality=lossless" alt="Requirements Icon"/>
</p>

*   **Spigot:** 1.17+ [Paper recommended]
*   **PlaceholderAPI:** 2.11.6+
*   **Vault:** 1.7+


<p align="center">
  <img src="https://media.discordapp.net/attachments/1339307861536276663/1341123336398438450/setup.png?ex=67b4d9f0&is=67b38870&hm=c37d8155b060e2b2cb8a0198a2797e453c1b49910b3ed70ab683111428d3eb8f&=&format=webp&quality=lossless" alt="Setup & Installation Icon"/>
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
  <img src="https://media.discordapp.net/attachments/1339307861536276663/1341123335945326592/cmd.png?ex=67b4d9f0&is=67b38870&hm=abcc5890d29aea1eff1836efd434c5d4de42fdb83cbdaaf98ceb66461bdc5ff0&=&format=webp&quality=lossless" alt="Slash Command Icon"/>
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
  <img src="https://media.discordapp.net/attachments/1339307861536276663/1341123337316864033/place.png?ex=67b4d9f0&is=67b38870&hm=8a00c188e29b9c8c4b831c4be9876b8477dbc35fad8198ba0173f7857ea0fec9&=&format=webp&quality=lossless" alt="Placeholders Icon"/>
</p>

#### For Giveaway and Giveaway End Embeds:
*   `{TIME-LEFT}`, `{END-TIME}`, `{ENTRIES}`, `{WIN-COUNT}`,
    `{PRIZE}`, `{WINNERS}`

#### For Log Embed:
*   `{GIVEAWAY-NAME}`, `{ENTRIES-COUNT}`, `{PRIZE}`, `{COMMANDS}`,
    `{WINNERS-MENTIONS}`, `{ENTRIES-LIST}`


<p align="center">
  <img src="https://media.discordapp.net/attachments/1339307861536276663/1341123336867938437/info.png?ex=67b4d9f0&is=67b38870&hm=46493436d8eadc6695e7b10c9ce6e24494be56b6b4af25f8a9111761ec22e9c8&=&format=webp&quality=lossless" alt="Important Notes Icon"/>
</p>

*   After making any changes to the `config.yml` file, remember to use `/mgwreload` in-game to reload the plugin.
    Sometimes, the plugin pauses itself because of runtime errors. To restart it just reload it ;)


[![bStats: Paper Servers](https://bstats.org/signatures/bukkit/mGiveaway.svg)](https://bstats.org/plugin/bukkit/mGiveaway/24362)
