![](https://i.imgur.com/Us7oVqO.png)

# EvilBlock-2FA
A two-factor authentication (OTP) implementation for your Minecraft server. Built with ease-of-use in mind, you can setup 2FA for your server's moderators and or players in a matter of minutes.

### Features
* Supports flat-file (JSON) and MongoDB out of the box
* Extensive configuration (made for dummies)
* Displays QR Code image on map that works with 2FA apps
* Configurable issuer name
* Prevents locked players from interacting with the world, chat, etc
* Prevents damage to locked players (by default)
* Whitelisted commands for locked players

### Install this plugin
1. Download the [latest release](https://github.com/joeleoli/EvilBlock-2FA/releases).
2. Drag and drop the file into your server's `plugins` folder.
3. Start your server and let it load completely.
4. Stop your server.
5. Review the newly generated configurations `config.yml` and `lang.yml` in your server's `plugins` directory.
6. Save any changes and re-start your server.

### Options
This is the default options configuration, found at `plugins/EvilBlock-2FA/config.yml`.

```
# Database implementation
database:
  # for multi-server networks, use "MONGO"
  # for single-server, use "JSON" (aka flat-file)
  implementation: "JSON"
  options:
    host: "127.0.0.1"
    port: 27017
    dbName: "two-factor-auth"
    authentication:
      enabled: false
      username: "admin"
      password: ""
settings:
  # The name that appears in the 2FA app
  issuer-name: "EvilBlock"
  # If only players with the '2fa.access' permission are allowed to setup 2FA
  require-permission: true
  # If players with the '2fa.access' permission are required to setup 2FA if they haven't already
  require-setup: true
  # The commands that players are allowed to execute while not verified
  whitelisted-commands:
    - "auth"
    - "2fasetup"
    - "setup2fa"
  # The text the player must enter to agree to the setup terms
  agree-text: "yes"
  # If the bypass command can only be executed by the console
  bypass-command-op-only: true
  # If players with the `2fa.bypass` permission are granted identity verification bypass
  use-bypass-permission: false
  # If damage to players in the locked state is disabled
  disable-damage-to-locked-players: false
```

### Language
This is the default language configuration, found at `plugins/EvilBlock-2FA/lang.yml`.

```
provide-code-prompt:
  - "&cPlease provide your two-factor code. Type \"/auth <code>\" to authenticate."
required-setup-prompt:
  - "&cPlease set up your two-factor authentication using \"/2fasetup\"."
disclaimer-prompt:
  - "&c&lTake a minute to read over this, it's important. 2FA can be enabled to protect against hackers getting into your Minecraft account. If you enable 2FA, you'll be required to enter a code every time you log in. If you lose your 2FA device, you won't be able to log in to the network."
  - "&7If you've read the above and would like to proceed, type \"yes\" in chat. Otherwise, type anything else."
scan-prompt:
  - "&cOn your 2FA device, scan the map given to you. Once you've scanned the map, type the code displayed on your device in chat."
setup-complete:
  - "&aYour 2FA is now setup."
setup-cancelled:
  - "&cCancelling 2FA setup due to too many incorrect codes."
  - "&cContact the staff team for any questions you have about 2FA."
setup-aborted:
  - "&aAborted 2FA setup."
```

### Permissions
* `2fa.access` - Grants access to the 2FA commands
* `2fa.bypass` - Grants identity verification bypass (disabled by default)
* `2fa.bypass.cmd` - Grants access to the bypass command
