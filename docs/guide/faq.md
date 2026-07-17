# FAQ

## Will MistTags override LuckPerms?

Only when a player has an active MistTags tag.

With smart placeholders, MistTags shows its own prefix/suffix first. If the player has no MistTags prefix/suffix, it falls back to LuckPerms.

## Will MistTags override resource packs?

No. MistTags only changes text prefixes/suffixes and placeholders. It does not touch resource packs, textures, models, sounds, fonts, or server resource-pack settings.

## Do animated prefixes animate in chat?

Not live. Chat messages are old text lines. LPC or another chat plugin asks PlaceholderAPI for the value when the message is sent, so the message keeps that frame forever.

Tab list and nametags can refresh if the display plugin supports frequent placeholder updates.

## Why is Paper complaining about LuckPerms Velocity?

The Velocity LuckPerms jar is for a Velocity proxy, not a Paper server.

MistTags can ignore it, but Paper still tries to load every jar in `plugins`. Remove the Velocity jar from the Paper server's `plugins` folder for clean logs.

## Do I need emoji encoding?

For YAML, normal UTF-8 files are fine.

For MySQL/MariaDB, use `utf8mb4` so emojis save correctly.

## Can I edit every plugin message?

Yes. MistTags messages are in `plugins/MistTags/msg.yml`.

## Does MistTags support Folia?

MistTags declares Folia support and uses scheduler reflection for Folia/Paper-compatible tasks. It is still a beta build, so test on your exact server jar before marking it stable.
