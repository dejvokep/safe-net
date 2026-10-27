# Security Policy

## Supported versions

Only the latest release receives security fixes. Please upgrade before reporting, if you can.

## Reporting a vulnerability

**Please do not report security issues in public GitHub issues, pull requests, on Discord or in the SpigotMC discussion and reviews.** A public report of a bypass puts every network running SafeNET at risk until a fix is released.

Report them privately through one of these channels:

- **GitHub** (preferred): open a report via [Report a vulnerability](https://github.com/dejvokep/safe-net/security/advisories/new) on this repository's Security tab.
- **Email**: [admin@dejvokep.dev](mailto:admin@dejvokep.dev), with `[SafeNET security]` in the subject.

Please include:

- the SafeNET version, and the proxy and backend software with their versions and builds (e.g. BungeeCord build #2000, Paper 1.21.4 build 230);
- whether ProtocolLib, GeyserMC or Floodgate are installed, and their versions;
- your SafeNET configuration on the proxy and the backend, with the **passphrase removed**;
- step-by-step reproduction, or a proof of concept (a client, packet sequence or script);
- the impact you observed or expect (e.g. joining a backend without going through the proxy, spoofing another player's UUID, bypassing the session check, leaking the passphrase).

**Never send your real passphrase.** If you think it has leaked, generate a new one with `/sn generate` on the proxy and copy it to all backend servers.

## What to expect

SafeNET is maintained by a single person, so these are targets rather than guarantees:

- acknowledgement within **7 days**;
- an initial assessment within **14 days**;
- a fix or mitigation released as soon as practical, on GitHub and [SpigotMC](https://www.spigotmc.org/resources/65075/), followed by a GitHub Security Advisory.

You will be kept informed of progress and credited in the advisory unless you prefer to stay anonymous. Please give us a reasonable time to release a fix before disclosing the issue publicly, since server owners need time to update.

## Scope

In scope:

- ways to join a backend server, or to be treated as authenticated by it, without connecting through a proxy running SafeNET with the correct passphrase;
- bypasses of the session check, including connections that reach the world before the handshake is verified;
- leaks of the passphrase or the session key to players or other servers;
- player identity spoofing (UUID, name or skin properties) through the forwarded profile, including the GeyserMC and Floodgate handling;
- crashes or denial of service that any player can trigger with crafted handshake or login data.

Out of scope:

- the **address whitelist**. As the configuration says, it is not a security feature;
- setups where SafeNET is not installed on every server of the network, or where the proxy and backends don't share the same passphrase and handshake property name;
- a passphrase that was leaked or shared by the server owner;
- proxies older than the supported BungeeCord build (see the README) and unmaintained server forks;
- data written to the console with `log-connection-data: true`, which is documented as including sensitive data;
- vulnerabilities in BungeeCord, Spigot, Paper, ProtocolLib, GeyserMC or Floodgate themselves. Please report those upstream, but do tell us if SafeNET should work around them.

## Hardening

SafeNET is a fallback for networks that can't control their firewall. If you can, also block direct access to your backend servers at the firewall, so that only the proxy can reach them, and keep `block-pings: true` where your server supports it. Change the passphrase from time to time with `/sn generate`.