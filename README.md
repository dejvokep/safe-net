# 🔒 SafeNET

[SafeNET](https://www.spigotmc.org/resources/65075/) is a lightweight plugin protecting you from direct backend server
access and IP-forwarding bypass exploit, mainly for those who cannot access or configure their firewall systems. The
plugin has been serving protection for everyone since 2019,
connecting [BungeeGuard](https://github.com/lucko/BungeeGuard) (from which it took the inspiration) and the good old
IP-whitelisting feature and delivering all features in one, compact and versatile package.

# ❓ What is IP-forwarding bypass?

When using a proxy server (e.g. BungeeCord) to connect your servers together, the backend servers must run in offline
mode, disabling account authentication. That enables hackers and unauthorized users to join backend servers with
whatever account freely (including yours as an admin).

Each player has their own profile, which contains information about them (UUID, skin textures...). These data are given
to the proxy server when a player joins and then forwarded by the proxy to each of the backend servers, when you're
being connected to them. This plugin uses a passphrase, which is forwarded to the backend servers to check for
integrity.

After an exploit has been found, which allows for packets to be uncaught by plugins during a specific timeframe,
effectively bypassing the authentication and leaving your server vulnerable, this system's been enriched with sessions.
A special session key is generated each time the server starts, is attached to player's profile when authenticated and
verified when the player is on the edge of joining the server (spawning into the world). Sessions patch any possible way
around the initial authentication.

# 👍 Why to use SafeNET over other plugins?

You can't go wrong with classic [BungeeGuard](https://github.com/lucko/BungeeGuard), however, if you would also like to
use the IP-whitelist, this is the way to go. The plugin also supports [GeyserMC](https://github.com/GeyserMC/Geyser) (
including [Floodgate](https://github.com/GeyserMC/Floodgate/)), which other plugins do not. Logs everything to the
console for easy verification and control over connection flow.

The plugin has been downloaded 20K+ times, and is active on 300+ networks managing over 1500 backend servers.

# 🔧 Setup and support

> **NOTE!** SafeNET should only be used if you cannot access, set up nor manage a firewall on your network (shared
> hosts).

Download the latest release from the panel on the right. The plugin supports **all major**:

- proxy servers (BungeeCord, Waterfall, FlameCord...) running **the latest release** (build [#1984](https://ci.md-5.net/job/BungeeCord/1984/) or newer),
- backend servers (Bukkit, Spigot, Paper, Purpur...) running **1.8+ releases**.

The plugin must be installed on all servers on your network. Consult the documentation linked below for further
information and setup instructions.

You can view the setup instructions and other details on [the wiki](https://dejvokep.gitbook.io/safenet/). If you need
help with anything,
feel free join the [Discord server](https://discord.gg/BbhADEy). Or, just to talk with us 👋
