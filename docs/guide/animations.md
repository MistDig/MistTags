# Animation Presets

Copy any preset into `plugins/MistTags/animations.yml` under the existing `animations:` section.

Use it in-game with:

```text
/mt addprefix <player> 30m anim:rainbow_vip VIP
/mt addsuffix <player> 30m anim:spark_staff STAFF
/mt preview anim:rainbow_vip VIP
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
      - "<red>{text}</red>"
      - "<gold>{text}</gold>"
      - "<yellow>{text}</yellow>"
      - "<green>{text}</green>"
      - "<aqua>{text}</aqua>"
      - "<blue>{text}</blue>"
      - "<light_purple>{text}</light_purple>"

  spark_staff:
    update-ticks: 6
    frames:
      - "<yellow>*</yellow><gold>{text}</gold><yellow>*</yellow>"
      - "<white>*</white><yellow>{text}</yellow><white>*</white>"
      - "<gold>*</gold><white>{text}</white><gold>*</gold>"
      - "<white>*</white><yellow>{text}</yellow><white>*</white>"

  pulse_admin:
    update-ticks: 8
    frames:
      - "<dark_red><bold>{text}</bold></dark_red>"
      - "<red><bold>{text}</bold></red>"
      - "<white><bold>{text}</bold></white>"
      - "<red><bold>{text}</bold></red>"

  ocean_mod:
    update-ticks: 7
    frames:
      - "<dark_aqua>{text}</dark_aqua>"
      - "<aqua>{text}</aqua>"
      - "<blue>{text}</blue>"
      - "<aqua>{text}</aqua>"

  emerald_helper:
    update-ticks: 8
    frames:
      - "<dark_green>{text}</dark_green>"
      - "<green>{text}</green>"
      - "<white>{text}</white>"
      - "<green>{text}</green>"

  gold_mvp:
    update-ticks: 6
    frames:
      - "<gold>{text}</gold>"
      - "<yellow>{text}</yellow>"
      - "<white>{text}</white>"
      - "<yellow>{text}</yellow>"

  royal_owner:
    update-ticks: 8
    frames:
      - "<dark_purple><bold>{text}</bold></dark_purple>"
      - "<light_purple><bold>{text}</bold></light_purple>"
      - "<white><bold>{text}</bold></white>"
      - "<light_purple><bold>{text}</bold></light_purple>"

  ice_elite:
    update-ticks: 5
    frames:
      - "<white>{text}</white>"
      - "<aqua>{text}</aqua>"
      - "<blue>{text}</blue>"
      - "<aqua>{text}</aqua>"

  fire_legend:
    update-ticks: 5
    frames:
      - "<dark_red>{text}</dark_red>"
      - "<red>{text}</red>"
      - "<gold>{text}</gold>"
      - "<yellow>{text}</yellow>"
      - "<gold>{text}</gold>"
      - "<red>{text}</red>"

  neon_plus:
    update-ticks: 4
    frames:
      - "<green>{text}</green>"
      - "<aqua>{text}</aqua>"
      - "<light_purple>{text}</light_purple>"
      - "<yellow>{text}</yellow>"

  wave_vip:
    update-ticks: 5
    frames:
      - "<white>[</white><green>{text}</green><white>]</white>"
      - "<green>[</green><white>{text}</white><green>]</green>"
      - "<white>[</white><aqua>{text}</aqua><white>]</white>"
      - "<aqua>[</aqua><white>{text}</white><aqua>]</aqua>"

  warning_staff:
    update-ticks: 10
    frames:
      - "<yellow>{text}</yellow>"
      - "<gold>{text}</gold>"
      - "<red>{text}</red>"
      - "<gold>{text}</gold>"

  soft_media:
    update-ticks: 8
    frames:
      - "<light_purple>{text}</light_purple>"
      - "<white>{text}</white>"
      - "<aqua>{text}</aqua>"
      - "<white>{text}</white>"

  midnight_dev:
    update-ticks: 8
    frames:
      - "<dark_blue>{text}</dark_blue>"
      - "<blue>{text}</blue>"
      - "<dark_aqua>{text}</dark_aqua>"
      - "<blue>{text}</blue>"

  silver_builder:
    update-ticks: 8
    frames:
      - "<gray>{text}</gray>"
      - "<white>{text}</white>"
      - "<dark_gray>{text}</dark_gray>"
      - "<white>{text}</white>"

  heart_donor:
    update-ticks: 7
    frames:
      - "<red><bold>{text}</bold></red>"
      - "<light_purple><bold>{text}</bold></light_purple>"
      - "<white><bold>{text}</bold></white>"
      - "<light_purple><bold>{text}</bold></light_purple>"

  lime_member:
    update-ticks: 10
    frames:
      - "<dark_green>{text}</dark_green>"
      - "<green>{text}</green>"
      - "<yellow>{text}</yellow>"
      - "<green>{text}</green>"

  aqua_champion:
    update-ticks: 6
    frames:
      - "<aqua>{text}</aqua>"
      - "<white>{text}</white>"
      - "<blue>{text}</blue>"
      - "<white>{text}</white>"

  ruby_knight:
    update-ticks: 6
    frames:
      - "<dark_red>{text}</dark_red>"
      - "<red>{text}</red>"
      - "<gray>{text}</gray>"
      - "<red>{text}</red>"

  clean_verified:
    update-ticks: 12
    frames:
      - "<green>{text}</green>"
      - "<white>{text}</white>"
      - "<green>{text}</green>"

  party_star:
    update-ticks: 4
    frames:
      - "<yellow>*</yellow><red>{text}</red><yellow>*</yellow>"
      - "<gold>*</gold><light_purple>{text}</light_purple><gold>*</gold>"
      - "<aqua>*</aqua><green>{text}</green><aqua>*</aqua>"
      - "<white>*</white><blue>{text}</blue><white>*</white>"

  dark_guard:
    update-ticks: 9
    frames:
      - "<dark_gray>{text}</dark_gray>"
      - "<gray>{text}</gray>"
      - "<white>{text}</white>"
      - "<gray>{text}</gray>"

  sunset_veteran:
    update-ticks: 6
    frames:
      - "<red>{text}</red>"
      - "<gold>{text}</gold>"
      - "<yellow>{text}</yellow>"
      - "<gold>{text}</gold>"

  frost_staff:
    update-ticks: 5
    frames:
      - "<white>{text}</white>"
      - "<aqua>{text}</aqua>"
      - "<dark_aqua>{text}</dark_aqua>"
      - "<aqua>{text}</aqua>"

  candy_vip:
    update-ticks: 5
    frames:
      - "<red>{text}</red>"
      - "<light_purple>{text}</light_purple>"
      - "<white>{text}</white>"
      - "<light_purple>{text}</light_purple>"

  toxic_plus:
    update-ticks: 5
    frames:
      - "<dark_green>{text}</dark_green>"
      - "<green>{text}</green>"
      - "<yellow>{text}</yellow>"
      - "<green>{text}</green>"

  sky_rank:
    update-ticks: 7
    frames:
      - "<blue>{text}</blue>"
      - "<aqua>{text}</aqua>"
      - "<white>{text}</white>"
      - "<aqua>{text}</aqua>"

  shadow_rank:
    update-ticks: 8
    frames:
      - "<black>{text}</black>"
      - "<dark_gray>{text}</dark_gray>"
      - "<gray>{text}</gray>"
      - "<dark_gray>{text}</dark_gray>"

  pride_simple:
    update-ticks: 4
    frames:
      - "<red>{text}</red>"
      - "<gold>{text}</gold>"
      - "<yellow>{text}</yellow>"
      - "<green>{text}</green>"
      - "<blue>{text}</blue>"
      - "<light_purple>{text}</light_purple>"

  chroma_rank:
    update-ticks: 3
    frames:
      - "<red>{text}</red>"
      - "<gold>{text}</gold>"
      - "<yellow>{text}</yellow>"
      - "<green>{text}</green>"
      - "<aqua>{text}</aqua>"
      - "<blue>{text}</blue>"
      - "<light_purple>{text}</light_purple>"
```

## Gradient Animations

Gradients use valid MiniMessage syntax:

```text
<gradient:#49d17d:#8fb6ff>{text}</gradient>
```

You can make an animation by switching between two or more gradients. Copy this block into `animations.yml` under `animations:`.

```yaml
animations:
  gradient_vip:
    update-ticks: 8
    frames:
      - "<gradient:#49d17d:#8fb6ff>{text}</gradient>"
      - "<gradient:#8fb6ff:#49d17d>{text}</gradient>"

  gradient_staff:
    update-ticks: 8
    frames:
      - "<gradient:#ff5555:#ffaa00><bold>{text}</bold></gradient>"
      - "<gradient:#ffaa00:#ffffff><bold>{text}</bold></gradient>"
      - "<gradient:#ffffff:#ff5555><bold>{text}</bold></gradient>"

  gradient_owner:
    update-ticks: 8
    frames:
      - "<gradient:#aa00aa:#ff55ff><bold>{text}</bold></gradient>"
      - "<gradient:#ff55ff:#ffffff><bold>{text}</bold></gradient>"
      - "<gradient:#ffffff:#aa00aa><bold>{text}</bold></gradient>"

  gradient_mod:
    update-ticks: 8
    frames:
      - "<gradient:#00aaaa:#55ffff>{text}</gradient>"
      - "<gradient:#55ffff:#5555ff>{text}</gradient>"
      - "<gradient:#5555ff:#00aaaa>{text}</gradient>"

  gradient_helper:
    update-ticks: 8
    frames:
      - "<gradient:#00aa00:#55ff55>{text}</gradient>"
      - "<gradient:#55ff55:#ffff55>{text}</gradient>"
      - "<gradient:#ffff55:#00aa00>{text}</gradient>"

  gradient_mvp:
    update-ticks: 7
    frames:
      - "<gradient:#ffaa00:#ffff55>{text}</gradient>"
      - "<gradient:#ffff55:#ffffff>{text}</gradient>"
      - "<gradient:#ffffff:#ffaa00>{text}</gradient>"

  gradient_elite:
    update-ticks: 7
    frames:
      - "<gradient:#ffffff:#55ffff>{text}</gradient>"
      - "<gradient:#55ffff:#5555ff>{text}</gradient>"
      - "<gradient:#5555ff:#ffffff>{text}</gradient>"

  gradient_legend:
    update-ticks: 6
    frames:
      - "<gradient:#aa0000:#ff5555>{text}</gradient>"
      - "<gradient:#ff5555:#ffaa00>{text}</gradient>"
      - "<gradient:#ffaa00:#ffff55>{text}</gradient>"
      - "<gradient:#ffff55:#ff5555>{text}</gradient>"

  gradient_media:
    update-ticks: 8
    frames:
      - "<gradient:#ff55ff:#55ffff>{text}</gradient>"
      - "<gradient:#55ffff:#ffffff>{text}</gradient>"
      - "<gradient:#ffffff:#ff55ff>{text}</gradient>"

  gradient_dev:
    update-ticks: 8
    frames:
      - "<gradient:#5555ff:#55ffff>{text}</gradient>"
      - "<gradient:#55ffff:#55ff55>{text}</gradient>"
      - "<gradient:#55ff55:#5555ff>{text}</gradient>"

  gradient_builder:
    update-ticks: 9
    frames:
      - "<gradient:#aaaaaa:#ffffff>{text}</gradient>"
      - "<gradient:#ffffff:#555555>{text}</gradient>"
      - "<gradient:#555555:#aaaaaa>{text}</gradient>"

  gradient_donor:
    update-ticks: 7
    frames:
      - "<gradient:#ff5555:#ff55ff>{text}</gradient>"
      - "<gradient:#ff55ff:#ffffff>{text}</gradient>"
      - "<gradient:#ffffff:#ff5555>{text}</gradient>"

  gradient_champion:
    update-ticks: 6
    frames:
      - "<gradient:#55ffff:#ffffff>{text}</gradient>"
      - "<gradient:#ffffff:#ffff55>{text}</gradient>"
      - "<gradient:#ffff55:#55ffff>{text}</gradient>"

  gradient_plus:
    update-ticks: 5
    frames:
      - "<gradient:#55ff55:#55ffff>{text}</gradient>"
      - "<gradient:#55ffff:#ff55ff>{text}</gradient>"
      - "<gradient:#ff55ff:#55ff55>{text}</gradient>"

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
