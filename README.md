# 🔒 SafeNET

[SafeNET](https://www.spigotmc.org/resources/65075/) is a lightweight plugin protecting you from direct backend server
access and IP-forwarding bypass exploit, mainly for those who cannot access or configure their firewall systems. The
plugin has been serving protection for everyone since 2019,
connecting [BungeeGuard](https://github.com/lucko/BungeeGuard) (from which it took the inspiration) and the good old
IP-whitelisting feature.

[![SafeNET is powered by Senior Hosting. Use code "YAML" for 10% off!](https://cdn.discordapp.com/attachments/927561782279675977/945372759289126973/Group_129.png)](https://senior-host.com/)

# ❓ What is IP-forwarding bypass?

When using a proxy server (e.g. BungeeCord) to connect your servers together, the backend servers must run in offline
mode, disabling account authentication. That enables hackers and unauthorized users to join backend servers with
whatever account freely (including yours as an admin).

Each player has their own profile, which contains information about them (UUID, skin textures...). These data are given
to the proxy server when a player joins and then forwarded by the proxy to each of the backend servers, when you're
being connected to them. This plugin uses a passphrase, which is inserted into the player's profile and then forwarded
to the backend servers to check for integrity.

After an exploit has been found, which allows for packets to be uncaught during a specific timeframe, effectively
bypassing the authentication and leaving your server vulnerable, this system's been enriched with sessions. A special
session key is generated each time the server starts, is attached to player's profile when authenticated and is verified
when the player is on the edge of joining the server (spawning into the world). Sessions patch any possible way around
the initial authentication.

# 👍 Why to use SafeNET over other plugins?

You can't go wrong with classic [BungeeGuard](https://github.com/lucko/BungeeGuard), however, if you would also like to
use the IP-whitelist, this is the way to go. The plugin also supports [GeyserMC](https://github.com/GeyserMC/Geyser) (
including [Floodgate](https://github.com/GeyserMC/Floodgate/)), which other plugins do not. Logs everything to the
console for easy verification and control over connection flow.

The plugin has been downloaded 14K+ times, and (on average) is active on 300+ networks managing over 1500 backend
servers.

# 🔧 Setup and support

Download the latest release from the panel on the right. **BungeeCord 1.19 and ProtocolLib 5.0.0 or newer are
required (if running [Paper servers](https://papermc.io/), ProtocolLib installation is not needed).** Alternatively,
use LEGACY version, which allows for use of older ProtocolLib releases. If you're stillusing version 3.5 or older,
**immediately upgrade** to at least 3.6.

| SafeNET | BungeeCord | ProtocolLib (not required if running [Paper servers](https://papermc.io/)) | Game |
| ------- | ---------- | ----------- | ---- |
| 3.7 | Latest build ([1637](https://ci.md-5.net/job/BungeeCord/1637/) or newer) | 5.0.0 (or newer) | 1.8 - 1.19 |
| 3.7-LEGACY | Latest build ([1637](https://ci.md-5.net/job/BungeeCord/1637/) or newer) | 4.8.0 (or older) | 1.8 - 1.18 |
| 3.5 (and older) | **VULNERABLE, DO NOT USE!** | - | - |

You can view the setup instructions at [the wiki](https://dejvokep.gitbook.io/safenet/). If you need help with anything,
feel free join the [Discord server](https://discord.gg/BbhADEy) (nonstop 24/7 🤖 support). Or, just to talk with us 👋
