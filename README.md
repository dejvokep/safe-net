# 🔒 SecuredNetwork

[SecuredNetwork](https://www.spigotmc.org/resources/65075/) is a lightweight plugin protecting you from IP-forwarding
bypass exploit, mainly for those who cannot access or configure their firewall systems. The plugin has been serving
protection for everyone since 2019, connecting [BungeeGuard](https://github.com/lucko/BungeeGuard) (from which it took
the inspiration) and the good old IP-whitelisting feature.

[![SecuredNetwork is powered by Senior Hosting. Use code "YAML" for 10% off!](https://cdn.discordapp.com/attachments/927561782279675977/945372759289126973/Group_129.png)](https://senior-host.com/)

# ❓ What is IP-forwarding bypass?

When using a proxy server (e.g. BungeeCord) to connect your servers together, the backend servers must run on offline
mode, disabling account authentication. That enables hackers to join backend servers with your account freely.

Each player has their own profile, which contains information about them (UUID, skin textures...). These data are given
to the proxy server when a player joins and then forwarded by the proxy to backend servers. This plugin uses a
passphrase, which is inserted into the player's profile and then forwarded to the backend servers to check for
integrity.

# 👍 Why to use SecuredNetwork over other plugins?

You can't go wrong with classic [BungeeGuard](https://github.com/lucko/BungeeGuard), however, if you would also like to
use the IP-whitelist, this is the way to go. The plugin also supports [GeyserMC](https://github.com/GeyserMC/Geyser) (including [Floodgate](https://github.com/GeyserMC/Floodgate/)), which other plugins do not. Logs everything to the
console for easy verification.

The plugin has been downloaded 10K+ times, and (on average) is active on 300+ networks managing over 1500 backend
servers.

# 🔧 Setup and support

You can view the setup instructions at [the wiki](https://dejvokep.gitbook.io/securednetwork/). If you need help with
anything, feel free join the [Discord server](https://discord.gg/BbhADEy) (nonstop 24/7 🤖 support). Or, just to talk
with us 👋
