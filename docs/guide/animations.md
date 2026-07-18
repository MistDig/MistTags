# Animation Presets

Copy any preset into `plugins/MistTags/animations.yml` under the existing `animations:` section.

Use it in-game with:

```text
/mt addprefix <player> 30m anim:rainbow_vip
/mt addsuffix <player> 30m anim:spark_staff
/mt preview anim:rainbow_vip
```

Template animations can use `{text}` in their frames. Then staff can provide the displayed text from the command:

```text
/mt addprefix MistDig 1m anim:gradient_chroma VIP
/mt addsuffix MistDig 1m anim:gradient_chroma IMMORTAL
/mt preview anim:gradient_chroma BUTTER
```

MistTags also accepts the colon form:

```text
/mt addprefix MistDig 1m anim:gradient_chroma:VIP
```

MistTags accepts safe MiniMessage color names such as `red`, `gold`, `yellow`, `green`, `aqua`, `blue`, `light_purple`, `white`, `gray`, `dark_gray`, `dark_red`, `dark_green`, `dark_aqua`, `dark_blue`, and `dark_purple`.

## Simple Color Animations

```yaml
animations:
  rainbow_vip:
    update-ticks: 4
    frames:
      - "<red>[VIP]</red>"
      - "<gold>[VIP]</gold>"
      - "<yellow>[VIP]</yellow>"
      - "<green>[VIP]</green>"
      - "<aqua>[VIP]</aqua>"
      - "<blue>[VIP]</blue>"
      - "<light_purple>[VIP]</light_purple>"

  spark_staff:
    update-ticks: 6
    frames:
      - "<yellow>*</yellow><gold>[STAFF]</gold><yellow>*</yellow>"
      - "<white>*</white><yellow>[STAFF]</yellow><white>*</white>"
      - "<gold>*</gold><white>[STAFF]</white><gold>*</gold>"
      - "<white>*</white><yellow>[STAFF]</yellow><white>*</white>"

  pulse_admin:
    update-ticks: 8
    frames:
      - "<dark_red><bold>[ADMIN]</bold></dark_red>"
      - "<red><bold>[ADMIN]</bold></red>"
      - "<white><bold>[ADMIN]</bold></white>"
      - "<red><bold>[ADMIN]</bold></red>"

  ocean_mod:
    update-ticks: 7
    frames:
      - "<dark_aqua>[MOD]</dark_aqua>"
      - "<aqua>[MOD]</aqua>"
      - "<blue>[MOD]</blue>"
      - "<aqua>[MOD]</aqua>"

  emerald_helper:
    update-ticks: 8
    frames:
      - "<dark_green>[HELPER]</dark_green>"
      - "<green>[HELPER]</green>"
      - "<white>[HELPER]</white>"
      - "<green>[HELPER]</green>"

  gold_mvp:
    update-ticks: 6
    frames:
      - "<gold>[MVP]</gold>"
      - "<yellow>[MVP]</yellow>"
      - "<white>[MVP]</white>"
      - "<yellow>[MVP]</yellow>"

  royal_owner:
    update-ticks: 8
    frames:
      - "<dark_purple><bold>[OWNER]</bold></dark_purple>"
      - "<light_purple><bold>[OWNER]</bold></light_purple>"
      - "<white><bold>[OWNER]</bold></white>"
      - "<light_purple><bold>[OWNER]</bold></light_purple>"

  ice_elite:
    update-ticks: 5
    frames:
      - "<white>[ELITE]</white>"
      - "<aqua>[ELITE]</aqua>"
      - "<blue>[ELITE]</blue>"
      - "<aqua>[ELITE]</aqua>"

  fire_legend:
    update-ticks: 5
    frames:
      - "<dark_red>[LEGEND]</dark_red>"
      - "<red>[LEGEND]</red>"
      - "<gold>[LEGEND]</gold>"
      - "<yellow>[LEGEND]</yellow>"
      - "<gold>[LEGEND]</gold>"
      - "<red>[LEGEND]</red>"

  neon_plus:
    update-ticks: 4
    frames:
      - "<green>[+]</green>"
      - "<aqua>[+]</aqua>"
      - "<light_purple>[+]</light_purple>"
      - "<yellow>[+]</yellow>"

  wave_vip:
    update-ticks: 5
    frames:
      - "<white>[</white><green>VIP</green><white>]</white>"
      - "<green>[</green><white>VIP</white><green>]</green>"
      - "<white>[</white><aqua>VIP</aqua><white>]</white>"
      - "<aqua>[</aqua><white>VIP</white><aqua>]</aqua>"

  warning_staff:
    update-ticks: 10
    frames:
      - "<yellow>[STAFF]</yellow>"
      - "<gold>[STAFF]</gold>"
      - "<red>[STAFF]</red>"
      - "<gold>[STAFF]</gold>"

  soft_media:
    update-ticks: 8
    frames:
      - "<light_purple>[MEDIA]</light_purple>"
      - "<white>[MEDIA]</white>"
      - "<aqua>[MEDIA]</aqua>"
      - "<white>[MEDIA]</white>"

  midnight_dev:
    update-ticks: 8
    frames:
      - "<dark_blue>[DEV]</dark_blue>"
      - "<blue>[DEV]</blue>"
      - "<dark_aqua>[DEV]</dark_aqua>"
      - "<blue>[DEV]</blue>"

  silver_builder:
    update-ticks: 8
    frames:
      - "<gray>[BUILDER]</gray>"
      - "<white>[BUILDER]</white>"
      - "<dark_gray>[BUILDER]</dark_gray>"
      - "<white>[BUILDER]</white>"

  heart_donor:
    update-ticks: 7
    frames:
      - "<red><bold>[DONOR]</bold></red>"
      - "<light_purple><bold>[DONOR]</bold></light_purple>"
      - "<white><bold>[DONOR]</bold></white>"
      - "<light_purple><bold>[DONOR]</bold></light_purple>"

  lime_member:
    update-ticks: 10
    frames:
      - "<dark_green>[MEMBER]</dark_green>"
      - "<green>[MEMBER]</green>"
      - "<yellow>[MEMBER]</yellow>"
      - "<green>[MEMBER]</green>"

  aqua_champion:
    update-ticks: 6
    frames:
      - "<aqua>[CHAMPION]</aqua>"
      - "<white>[CHAMPION]</white>"
      - "<blue>[CHAMPION]</blue>"
      - "<white>[CHAMPION]</white>"

  ruby_knight:
    update-ticks: 6
    frames:
      - "<dark_red>[KNIGHT]</dark_red>"
      - "<red>[KNIGHT]</red>"
      - "<gray>[KNIGHT]</gray>"
      - "<red>[KNIGHT]</red>"

  clean_verified:
    update-ticks: 12
    frames:
      - "<green>[VERIFIED]</green>"
      - "<white>[VERIFIED]</white>"
      - "<green>[VERIFIED]</green>"

  party_star:
    update-ticks: 4
    frames:
      - "<yellow>*</yellow><red>[PARTY]</red><yellow>*</yellow>"
      - "<gold>*</gold><light_purple>[PARTY]</light_purple><gold>*</gold>"
      - "<aqua>*</aqua><green>[PARTY]</green><aqua>*</aqua>"
      - "<white>*</white><blue>[PARTY]</blue><white>*</white>"

  dark_guard:
    update-ticks: 9
    frames:
      - "<dark_gray>[GUARD]</dark_gray>"
      - "<gray>[GUARD]</gray>"
      - "<white>[GUARD]</white>"
      - "<gray>[GUARD]</gray>"

  sunset_veteran:
    update-ticks: 6
    frames:
      - "<red>[VETERAN]</red>"
      - "<gold>[VETERAN]</gold>"
      - "<yellow>[VETERAN]</yellow>"
      - "<gold>[VETERAN]</gold>"

  frost_staff:
    update-ticks: 5
    frames:
      - "<white>[STAFF]</white>"
      - "<aqua>[STAFF]</aqua>"
      - "<dark_aqua>[STAFF]</dark_aqua>"
      - "<aqua>[STAFF]</aqua>"

  candy_vip:
    update-ticks: 5
    frames:
      - "<red>[VIP]</red>"
      - "<light_purple>[VIP]</light_purple>"
      - "<white>[VIP]</white>"
      - "<light_purple>[VIP]</light_purple>"

  toxic_plus:
    update-ticks: 5
    frames:
      - "<dark_green>[PLUS]</dark_green>"
      - "<green>[PLUS]</green>"
      - "<yellow>[PLUS]</yellow>"
      - "<green>[PLUS]</green>"

  sky_rank:
    update-ticks: 7
    frames:
      - "<blue>[SKY]</blue>"
      - "<aqua>[SKY]</aqua>"
      - "<white>[SKY]</white>"
      - "<aqua>[SKY]</aqua>"

  shadow_rank:
    update-ticks: 8
    frames:
      - "<black>[SHADOW]</black>"
      - "<dark_gray>[SHADOW]</dark_gray>"
      - "<gray>[SHADOW]</gray>"
      - "<dark_gray>[SHADOW]</dark_gray>"

  pride_simple:
    update-ticks: 4
    frames:
      - "<red>[PRIDE]</red>"
      - "<gold>[PRIDE]</gold>"
      - "<yellow>[PRIDE]</yellow>"
      - "<green>[PRIDE]</green>"
      - "<blue>[PRIDE]</blue>"
      - "<light_purple>[PRIDE]</light_purple>"

  chroma_rank:
    update-ticks: 3
    frames:
      - "<red>[CHROMA]</red>"
      - "<gold>[CHROMA]</gold>"
      - "<yellow>[CHROMA]</yellow>"
      - "<green>[CHROMA]</green>"
      - "<aqua>[CHROMA]</aqua>"
      - "<blue>[CHROMA]</blue>"
      - "<light_purple>[CHROMA]</light_purple>"
```

## Gradient Animations

Gradients use valid MiniMessage syntax:

```text
<gradient:#49d17d:#8fb6ff>[VIP]</gradient>
```

You can make an animation by switching between two or more gradients. Copy this block into `animations.yml` under `animations:`.

```yaml
animations:
  gradient_vip:
    update-ticks: 8
    frames:
      - "<gradient:#49d17d:#8fb6ff>[VIP]</gradient>"
      - "<gradient:#8fb6ff:#49d17d>[VIP]</gradient>"

  gradient_staff:
    update-ticks: 8
    frames:
      - "<gradient:#ff5555:#ffaa00><bold>[STAFF]</bold></gradient>"
      - "<gradient:#ffaa00:#ffffff><bold>[STAFF]</bold></gradient>"
      - "<gradient:#ffffff:#ff5555><bold>[STAFF]</bold></gradient>"

  gradient_owner:
    update-ticks: 8
    frames:
      - "<gradient:#aa00aa:#ff55ff><bold>[OWNER]</bold></gradient>"
      - "<gradient:#ff55ff:#ffffff><bold>[OWNER]</bold></gradient>"
      - "<gradient:#ffffff:#aa00aa><bold>[OWNER]</bold></gradient>"

  gradient_mod:
    update-ticks: 8
    frames:
      - "<gradient:#00aaaa:#55ffff>[MOD]</gradient>"
      - "<gradient:#55ffff:#5555ff>[MOD]</gradient>"
      - "<gradient:#5555ff:#00aaaa>[MOD]</gradient>"

  gradient_helper:
    update-ticks: 8
    frames:
      - "<gradient:#00aa00:#55ff55>[HELPER]</gradient>"
      - "<gradient:#55ff55:#ffff55>[HELPER]</gradient>"
      - "<gradient:#ffff55:#00aa00>[HELPER]</gradient>"

  gradient_mvp:
    update-ticks: 7
    frames:
      - "<gradient:#ffaa00:#ffff55>[MVP]</gradient>"
      - "<gradient:#ffff55:#ffffff>[MVP]</gradient>"
      - "<gradient:#ffffff:#ffaa00>[MVP]</gradient>"

  gradient_elite:
    update-ticks: 7
    frames:
      - "<gradient:#ffffff:#55ffff>[ELITE]</gradient>"
      - "<gradient:#55ffff:#5555ff>[ELITE]</gradient>"
      - "<gradient:#5555ff:#ffffff>[ELITE]</gradient>"

  gradient_legend:
    update-ticks: 6
    frames:
      - "<gradient:#aa0000:#ff5555>[LEGEND]</gradient>"
      - "<gradient:#ff5555:#ffaa00>[LEGEND]</gradient>"
      - "<gradient:#ffaa00:#ffff55>[LEGEND]</gradient>"
      - "<gradient:#ffff55:#ff5555>[LEGEND]</gradient>"

  gradient_media:
    update-ticks: 8
    frames:
      - "<gradient:#ff55ff:#55ffff>[MEDIA]</gradient>"
      - "<gradient:#55ffff:#ffffff>[MEDIA]</gradient>"
      - "<gradient:#ffffff:#ff55ff>[MEDIA]</gradient>"

  gradient_dev:
    update-ticks: 8
    frames:
      - "<gradient:#5555ff:#55ffff>[DEV]</gradient>"
      - "<gradient:#55ffff:#55ff55>[DEV]</gradient>"
      - "<gradient:#55ff55:#5555ff>[DEV]</gradient>"

  gradient_builder:
    update-ticks: 9
    frames:
      - "<gradient:#aaaaaa:#ffffff>[BUILDER]</gradient>"
      - "<gradient:#ffffff:#555555>[BUILDER]</gradient>"
      - "<gradient:#555555:#aaaaaa>[BUILDER]</gradient>"

  gradient_donor:
    update-ticks: 7
    frames:
      - "<gradient:#ff5555:#ff55ff>[DONOR]</gradient>"
      - "<gradient:#ff55ff:#ffffff>[DONOR]</gradient>"
      - "<gradient:#ffffff:#ff5555>[DONOR]</gradient>"

  gradient_champion:
    update-ticks: 6
    frames:
      - "<gradient:#55ffff:#ffffff>[CHAMPION]</gradient>"
      - "<gradient:#ffffff:#ffff55>[CHAMPION]</gradient>"
      - "<gradient:#ffff55:#55ffff>[CHAMPION]</gradient>"

  gradient_plus:
    update-ticks: 5
    frames:
      - "<gradient:#55ff55:#55ffff>[PLUS]</gradient>"
      - "<gradient:#55ffff:#ff55ff>[PLUS]</gradient>"
      - "<gradient:#ff55ff:#55ff55>[PLUS]</gradient>"

  gradient_chroma:
    update-ticks: 4
    frames:
      - "<gradient:#ff5555:#ffaa00>{text}</gradient>"
      - "<gradient:#ffaa00:#ffff55>{text}</gradient>"
      - "<gradient:#ffff55:#55ff55>{text}</gradient>"
      - "<gradient:#55ff55:#55ffff>{text}</gradient>"
      - "<gradient:#55ffff:#5555ff>{text}</gradient>"
      - "<gradient:#5555ff:#ff55ff>{text}</gradient>"
```
