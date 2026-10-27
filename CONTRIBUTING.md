# Contributing to SafeNET

Thanks for your interest in improving SafeNET! Bug reports, fixes, documentation and features are all welcome. This guide explains how to get your change merged with as little back-and-forth as possible.

- **Questions and help:** join the [Discord server](https://discord.gg/BbhADEy) or read the [wiki](https://dejvokep.gitbook.io/safenet/).
- **Security issues** (anything that lets a player bypass SafeNET): do **not** open a public issue. Follow [SECURITY.md](SECURITY.md) instead.

## Before you start

- **Bugs:** search the [existing issues](https://github.com/dejvokep/safe-net/issues) first. If none matches, open one with the SafeNET version, the proxy and backend software with their versions, the installed ProtocolLib, GeyserMC or Floodgate versions, the console log, and the expected vs. actual result. Never include your passphrase.
- **Small fixes** (typos, messages, Javadoc, obvious bugs): open a pull request directly.
- **Features, behaviour changes and refactors:** open an issue first and describe what you want to change and why. SafeNET runs on hundreds of networks and every change to the authentication flow is a potential bypass, so agreeing on the approach up front saves you from rewriting the PR.

## Project layout

| Module | Contents |
| ------ | -------- |
| `safe-net-core` | Code shared by both platforms, such as the passphrase vault and key generator. |
| `safe-net-bungeecord` | The proxy plugin: attaches the passphrase to the forwarded profile, the address whitelist and the update checker. |
| `safe-net-spigot` | The backend plugin: verifies the handshake (via Paper events or ProtocolLib on Spigot) and the session before the player joins. |
| `safe-net-distribution` | Shades all modules into the single jar that is installed on both the proxy and the backends. |

## Building

You need **JDK 25 or newer**, because the Paper API it compiles against requires it. The plugin itself is still compiled to Java 8 bytecode. The Maven Wrapper downloads the right Maven version, so you don't need Maven installed.

```bash
./mvnw verify        # Linux, macOS, Git Bash
mvnw verify          # Windows (cmd), or .\mvnw verify in PowerShell
```

`verify` compiles all modules, checks license headers and builds the plugin jar at `target/SafeNET-<version>.jar` in the repository root. It needs no credentials.

CI runs the same build for every pull request and push to `main`. It also checks that every class in the jar is Java 8 bytecode and that bundled libraries are relocated, and uploads the jar as a build artifact. You can download it from the workflow run to test a PR without building it yourself.

## Making changes

### Compatibility

- The code must stay compatible with **Java 8**, because SafeNET supports backend servers from 1.8 onwards. Don't use language features or JDK APIs newer than Java 8.
    - `safe-net-core` and `safe-net-bungeecord` compile with `--release 8`, so the build fails if you use a newer JDK API there.
    - `safe-net-spigot` can't, because it calls record accessors on newer AuthLib versions. It compiles against the full JDK 25 API, so the compiler won't stop you from calling, say, `String.isBlank()`, which then fails on Java 8 servers at runtime. Check the Javadoc's `@since` for any JDK method you use there.
- On the backend, the same jar runs on Spigot and Paper, old and new. Use only API that exists on the oldest supported version, or check for it at runtime and fall back, as the Paper and Spigot (ProtocolLib) handshake listeners do.
- On the proxy, target the supported BungeeCord build and its forks (Waterfall, FlameCord...).
- Configuration changes must keep existing config files working. When you add or rename a key, bump `config-version` in the default config, so that BoostedYAML updates users' files automatically.

### Testing

There is no automated test suite yet, so changes are tested on a real network. In the PR description, list what you tested on. For anything touching authentication, at a minimum:

- a BungeeCord (or fork) proxy and at least one Paper and one Spigot backend, all running your build;
- one recent version and 1.8, if your change affects the backend;
- that a player can join through the proxy, and is disconnected when connecting to the backend directly or with a wrong passphrase;
- with GeyserMC/Floodgate, if your change touches profile or hostname handling.

Unit tests for the core module (JUnit 5 in `src/test/java`, mirroring the package of the class they test) are very welcome.

### Security-sensitive code

Take extra care with the handshake and session listeners, the authenticator and the passphrase handling:

- Fail closed: if anything is missing, malformed or unexpected, disconnect the player rather than letting them through.
- Never log or send the passphrase or session key to players, and don't add new logging of connection data unless `log-connection-data` is enabled.
- Generate secrets with `SecureRandom` (see `KeyGenerator`), and compare them with `MessageDigest.isEqual` rather than `String.equals` in new code.
- Explain in the PR why the change can't open a new way around the checks.

### Keep the diff focused

- Change only what your PR is about. Don't reformat, reorder or rename unrelated code.
- **License headers:** every `.java` and `.xml` file needs the header from `LICENSE-header.txt`; the build fails if one is missing. Add it to new files with `./mvnw license:format`, but don't commit header changes to files you didn't otherwise touch.
- Match the surrounding code style: 4-space indentation, Javadoc on classes and public methods, `@NotNull` / `@Nullable` (from `org.jetbrains.annotations`) on parameters and return values, and comments in the same style as the surrounding code.
- Keep player-facing messages in the config files rather than hard-coded.
- **Dependencies:** prefer what the server already provides (such as Gson on Spigot) and declare it `provided`. A library that has to be bundled must be shaded and relocated under `dev.dejvokep.safenet`, like BoostedYAML and bStats, so it can't clash with other plugins.

## Pull requests

- Target the `dev` branch, and keep one topic per PR. `dev` is merged into `main` for each release.
- Use short, imperative commit messages, e.g. `Fix session check on Paper 1.8`.
- In the description, explain what changed and why, link the issue (`Fixes #123`), and list what you tested on.
- CI must pass.
- If you change behaviour or configuration, say so explicitly, including how existing users are affected.
- Pull requests waiting on changes from the author for more than 60 days may be closed. You're welcome to reopen them when you continue.

## License

SafeNET is licensed under the [Apache License 2.0](LICENSE). By submitting a contribution, you agree that it is licensed under the same license, as described in section 5 of the license. There is no separate contributor agreement.