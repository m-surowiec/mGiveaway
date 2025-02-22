<p align="center">
  <img src="https://i.imgur.com/r36XcWN.png" alt="mGiveaway Banner"/>
</p>

<p align="center" style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">
  Boost your server community with exciting giveaways!<br>
  mGiveaway seamlessly integrates Discord and Minecraft,<br>
  allowing you to create engaging giveaways, set custom rewards,<br>
  and automatically deliver prizes to winners in-game.
</p>

<br>

<p align="center">
  <a href="https://github.com/m-surowiec/mGiveaway">
    <img src="https://i.imgur.com/1jemm1e.png" alt="GitHub Link"/>
  </a>
  <a href="https://discord.gg/MtFgx2jnYE">
    <img src="https://i.imgur.com/nblc47G.png" alt="Discord Link"/>
  </a>
</p>

<p align="center">
  <h1 style="font-family: 'Trebuchet MS'; color: #00b3b3;">mGiveaway - Discord Minecraft Giveaway Plugin</h1>
</p>

<p align="center">
  <img src="https://i.imgur.com/zI6CWqZ.png" alt="Key Features Icon"/>
</p>

<ul style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">
  <li><b>Discord Integration:</b> Slash commands for easy giveaway creation & management in Discord.</li>
  <li><b>Automated Giveaways:</b> Schedule start/end times for fully automated giveaways.</li>
  <li><b>Configurable Requirements:</b> Permissions, Groups/Roles (Vault), PlaceholderAPI values.</li>
  <li><b>In-Game Rewards:</b> Automatic execution of commands for winners.</li>
  <li><b>Customizable Embeds:</b> JSON configuration for rich Discord embed messages.</li>
  <li><b>Dynamic Placeholders:</b> Embeds update with time left, entries, winners, etc.</li>
  <li><b>Giveaway Reminders:</b> In-game broadcasts to notify players.</li>
  <li><b>Statistics Tracking:</b> Track entries and wins.</li>
  <li><b>Extensive Text Config:</b>  <i>config.yml</i> for messages, UI text, etc.</li>
  <li><b>Update Checker:</b> In-game notifications for new versions.</li>
</ul>

<p align="center">
  <img src="https://i.imgur.com/feLp8Uj.png" alt="Requirements Icon"/>
</p>

<ul style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">
  <li><b>Spigot:</b> 1.17+ [Paper recommended]</li>
  <li><b>PlaceholderAPI:</b> 2.11.6+</li>
  <li><b>Vault:</b> 1.7+</li>
</ul>

<p align="center">
  <img src="https://i.imgur.com/zQFONmG.png" alt="Setup & Installation Icon"/>
</p>

<ol style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">
  <li><b>Download & Install:</b> Get <u>mGiveaway-VERSION.jar</u> from <a href="https://github.com/m-surowiec/mGiveaway/releases/latest">GitHub Releases</a> and place in <b>/plugins</b> folder.</li>
  <li><b>Restart your server</b></li>
  <li><b>Configure Discord Bot in <i>config.yml</i>:</b>
    <ul>
      <li>Create Bot at <a href="https://discord.com/developers/applications">Discord Developer Portal</a>.</li>
      <li>Get Bot Token ("Bot" tab).</li>
      <li>Paste Token into <b>discord.bot.token</b> in <b>plugins/mGiveaway/config.yml</b></li>
    </ul>
  </li>
  <li><b>Configure <i>config.yml</i> for:</b>
    <ul>
      <li><b>prefix:</b> In-game prefix.</li>
      <li><b>broadcast_interval</b> & <b>broadcast_message:</b> Giveaway reminders.</li>
      <li><b>discord.bot</b> settings (token, status, activity, channels, etc.) - <b>Token & giveaway_channel REQUIRED</b>.</li>
      <li><b>discord.bot.command</b> customization.</li>
      <li><b>discord.bot.giveaway_embed</b>, <b>giveaway_end_embed</b>, <b>log_embed:</b> JSON Embed customization (use <a href="https://glitchii.github.io/embedbuilder/">Discord Embed Builder</a>).</li>
      <li><b>messages:</b> All text messages.</li>
      <li><b>giveaways:</b> Define giveaway settings & requirements.</li>
    </ul>
  </li>
  <li><b>Reload Plugin:</b> Use <i>/mgwreload</i> or restart server.</li>
</ol>

<p align="center">
  <img src="https://i.imgur.com/mW79SAM.png" alt="Slash Command Icon"/>
</p>

<p style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">Use <i>/create_giveaway</i> (default) to create giveaways.</p>
<pre style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">/create_giveaway name:<giveaway_name> prize:<prize_description> minecraft_prize:<minecraft_prize_placeholder> duration:<duration_string> winners:<number_of_winners> command:<reward_command> requirements:<true/false></pre>

<ul style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">
  <li><b>name:</b> <b>REQUIRED:</b> Internal giveaway name.</li>
  <li><b>prize:</b> <b>REQUIRED:</b> Formatted prize description (Discord).</li>
  <li><b>minecraft_prize:</b> <b>REQUIRED:</b> Plain-text prize (in-game broadcast).</li>
  <li><b>duration:</b> <b>REQUIRED:</b> Duration string (e.g., <i>2d 30m</i>).</li>
  <li><b>winners:</b> <b>REQUIRED:</b> Number of winners.</li>
  <li><b>command:</b> <b>REQUIRED:</b> First reward command (<i>%player%</i> placeholder).</li>
  <li><b>requirements:</b> <b>OPTIONAL:</b> <i>true</i>/<i>false</i> (if the giveaway start should wait for requirements)</li>
</ul>

<h2 style="font-family: 'Trebuchet MS'; font-size: 24px; color: #00b3b3;">⚙️ Configuring Giveaways (<i>config.yml</i> - <i>giveaways</i> section):</h2>

<p style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">See the example giveaway configuration (<i>giveaways.5Diamonds</i>) in the <i>config.yml</i> for a template. Key configuration options for each giveaway defined under the <i>giveaways</i> section:</p>

<ul style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">
  <li><b>settings:</b> - Settings for the giveaway
    <ul>
      <li><b>scheduled_start:</b> (Optional) Date and time to automatically start the giveaway at a future time. Format: <i>dd/MM/yyyy HH:mm:ss</i> (24-hour format). If not set, the giveaway starts immediately (or when manually forced).</li>
      <li><b>end_time:</b> <b>REQUIRED:</b> Date and time to automatically end the giveaway. Format: <i>dd/MM/yyyy HH:mm:ss</i> (24-hour format).</li>
      <li><b>winners:</b> <b>REQUIRED:</b> The number of winners for this giveaway.</li>
      <li><b>commands:</b> <b>REQUIRED:</b> Reward commands (<i>%player%</i>).</li>
      <li><b>prize_formatted:</b> <b>REQUIRED:</b> Discord prize description.</li>
      <li><b>minecraft_prize:</b> <b>REQUIRED:</b> In-game prize text.</li>
    </ul>
  </li>
  <li><b>requirements:</b> (Optional)
    <ul>
      <li><b>group:</b> Vault Group/Role.
        <ul>
          <li><i>&lt;group_name&gt;</i>: <i>value: true/false</i>, <i>formatted</i>.</li>
        </ul>
      </li>
      <li><b>permission:</b> Vault Permission.
        <ul>
          <li><i>&lt;permission_node&gt;</i>: <i>value: true/false</i>, <i>formatted</i>.</li>
        </ul>
      </li>
      <li><b>placeholder:</b> PlaceholderAPI value.
        <ul>
          <li><i>&lt;placeholder_name&gt;</i>: <i>over: &lt;number&gt;</i> or <i>under: &lt;number&gt;</i>, <i>formatted</i>.</li>
        </ul>
      </li>
    </ul>
  </li>
</ul>

<p align="center">
  <img src="https://i.imgur.com/ncy99or.png" alt="Placeholders Icon"/>
</p>

<h3 style="font-family: 'Trebuchet MS'; font-size: 20px; color: #00b3b3;">For Giveaway and Giveaway End Embeds:</h3>
<p style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">    {TIME-LEFT}, {END-TIME}, {ENTRIES}, {WIN-COUNT}, {PRIZE}, {WINNERS}</p>

<h3 style="font-family: 'Trebuchet MS'; font-size: 20px; color: #00b3b3;">For Log Embed:</h3>
<p style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">    {GIVEAWAY-NAME}, {ENTRIES-COUNT}, {PRIZE}, {COMMANDS}, {WINNERS-MENTIONS}, {ENTRIES-LIST}</p>

<p align="center">
  <img src="https://i.imgur.com/Yum0oka.png" alt="Important Notes Icon"/>
</p>

<p align="center" style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;"><b>After making any changes to the <i>config.yml</i> file, remember to use <i>/mgwreload</i> in-game to reload the plugin.</b></p>
<p align="center" style="font-family: 'Trebuchet MS'; font-size: 18px; color: #333;">Sometimes, the plugin pauses itself because of runtime errors. To restart it just reload it ;)</p>

  [![bStats: Paper Servers](https://bstats.org/signatures/bukkit/mGiveaway.svg)]([https://bstats.org/plugin/24362](https://bstats.org/plugin/bukkit/mGiveaway/24362))
