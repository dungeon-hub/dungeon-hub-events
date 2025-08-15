# Dungeon Hub Battle Royale - Quest Master System

## Overview

The Quest Master System adds interactive NPCs to your Battle Royale world. Players can interact with these NPCs to access a **quest GUI with 5 unique quests per Quest Master**, offering rewards like buffs, items, or special abilities.

## Features

- **5 Static NPCs**: Designed for placement at 5 central temples
- **Quest GUI System**: Each Quest Master offers 5 interactive quests through a custom GUI
- **Quest Progress Tracking**: Players can track active and completed quests
- **Multiple Reward Types**: Items, buffs, abilities, and messages
- **Quest Requirements**: Level requirements, item requirements, prerequisite quests
- **Flexible Configuration**: Easy YAML-based setup for both NPCs and quests
- **Anti-Spam Protection**: One-time use or cooldown-based interactions
- **Admin Commands**: Full management through in-game commands

## Quick Start

1. **Build & Install**: Place the plugin JAR in your server's `plugins` folder
2. **Start Server**: The plugin will create default configurations
3. **Configure Locations**: Edit `plugins/battle-royale-event/questmasters.yml`
4. **Configure Quests**: Edit `plugins/battle-royale-event/quests.yml`
5. **Reload**: Use `/questmaster reload` to apply changes

## Quest GUI System

### How It Works

1. **Right-click** any Quest Master NPC to open their quest GUI
2. **Browse 5 quests** specific to that Quest Master's theme
3. **Click quests** to accept them or view requirements
4. **Track progress** through quest objectives
5. **Complete quests** to earn rewards

### Quest Status Indicators

- **🟢 Available** - Green name, ready to accept
- **🟡 In Progress** - Yellow name with progress indicator  
- **🔴 Completed** - Green checkmark, already finished
- **⚫ Locked** - Gray name, requirements not met

## Configuration

### Quest Masters (`questmasters.yml`)

```yaml
questmasters:
  temple_zeroth:
    name: "Quest Master Zeroth"
    world: "world"
    x: 100.0
    y: 70.0
    z: 100.0
    message: "Welcome to the Temple of Zeroth! I grant you swiftness."
    one-time-use: true
    cooldown: 0
    reward-type: "BUFF"
    reward-data: "SPEED:1:600"
```

### Quests (`quests.yml`)

```yaml
quest-masters:
  temple_zeroth:
    quests:
      1:
        id: "zeroth_1"
        name: "Swift Hunter"
        description:
          - "Eliminate 3 opponents quickly"
          - "to prove your speed and agility"
        icon: "IRON_SWORD"
        reward-type: "ITEM"
        reward-data: "POTION:1"
        requirements:
          level: 0
          completed-quests: []
          items: {}
        available: true
```

### Reward Types

- **NONE**: No reward, just interaction
- **ITEM**: Give items to players
  - Format: `"MATERIAL:AMOUNT"` (e.g., `"GOLDEN_APPLE:2"`)
- **BUFF**: Apply potion effects
  - Format: `"EFFECT:AMPLIFIER:DURATION"` (e.g., `"SPEED:1:600"`)
- **ABILITY**: Special abilities (same format as BUFF)
- **MESSAGE**: Display custom text

## Commands

All commands require the `dungeonhub.questmaster.admin` permission (default: OP).

### Quest Master Management
- `/questmaster list` - Show all Quest Masters
- `/questmaster spawn <quest_id>` - Spawn a Quest Master at your location
- `/questmaster remove <quest_id>` - Remove a Quest Master
- `/questmaster tp <quest_id>` - Teleport to a Quest Master
- `/questmaster info <quest_id>` - Show detailed information
- `/questmaster reload` - Reload configuration

### Quest GUI & Progress
- `/questmaster gui <quest_id>` - Open a Quest Master's GUI directly
- `/questmaster quest list` - Show your quest progress
- `/questmaster quest complete <quest_id>` - Complete a quest (admin only)

## Player Interaction

Players can:
1. **Right-click** any Quest Master to open their quest GUI
2. **Browse 5 themed quests** per Quest Master
3. **Accept quests** that meet their requirements
4. **Track progress** through quest objectives
5. **Complete quests** to earn rewards
6. **View quest history** and completed quests

## Default Quest Masters & Their Themes

The plugin creates 5 default Quest Masters, each with 5 themed quests:

### 1. **Temple Zeroth** (100, 70, 100) - Speed & Agility
- Swift Hunter - Combat elimination quest
- Speed Demon - Survival challenge
- Wind Walker - Movement challenge  
- Lightning Strike - Timed combat
- Master of Velocity - Master quest (requires completing others)

### 2. **Temple Primus** (-100, 70, 100) - Wealth & Resources
- Treasure Hunter - Collection quest
- Hoarder - Resource accumulation
- Wealthy Warrior - Spending challenge
- Golden Guardian - Protection quest
- Master of Wealth - Economics mastery

### 3. **Temple Secundus** (100, 70, -100) - Defense & Survival
- Defensive Stance - Shield usage
- Survivor - Damage resistance
- Iron Will - Endurance t est
- Damage Sponge - Absorption challenge
- Fortress Master - Defensive mastery

### 4. **Temple Tertius** (-100, 70, -100) - Combat & Weapons
- Blade Dancer - Melee combat
- Combo Master - Killstreak challenge
- Weapon Collector - Weapon diversity
- Duelist - PvP combat
- Grandmaster Warrior - Combat mastery

### 5. **Temple Quartus** (0, 80, 0) - Movement & Heights
- High Jumper - Exploration quest
- Aerial Ace - Airborne combat
- Parkour Master - Movement skill
- Sky Walker - Flight endurance
- Master of Heights - Aerial mastery

## Customization

### Adding New Quest Masters

1. Edit `questmasters.yml`
2. Add a new entry under `questmasters:`
3. Use `/questmaster reload` to apply changes

### Custom Rewards

**Powerful Items:**
```yaml
reward-type: "ITEM"
reward-data: "DIAMOND_CHESTPLATE:1"
```

**Long-term Buffs:**
```yaml
reward-type: "BUFF"
reward-data: "REGENERATION:1:1200"  # 1 minute of regeneration
```

**Special Messages:**
```yaml
reward-type: "MESSAGE"
reward-data: "You have been chosen by the ancient spirits!"
```

## Permissions

- `dungeonhub.questmaster.admin` - Full Quest Master management (default: OP)

## Technical Details

- **NPCs**: Invisible ArmorStands with custom names
- **Protection**: Invulnerable, no gravity, non-pushable
- **Storage**: Player progress stored in memory (resets on restart)
- **Performance**: Minimal impact, event-driven interactions

## Troubleshooting

**Quest Masters not spawning?**
- Check world name in config matches your actual world
- Verify coordinates are valid
- Use `/questmaster list` to see status

**Players can't interact?**
- Ensure NPCs are within 3 blocks of interaction
- Check if one-time use is enabled and already completed
- Verify cooldown hasn't expired

**Commands not working?**
- Check permissions (`dungeonhub.questmaster.admin`)
- Ensure you're using correct syntax
- Try `/questmaster` for help

## Integration

This system is designed to integrate with:
- Battle Royale game mechanics
- Custom world/map designs
- Event-based gameplay
- Progression systems

## Support

For issues or feature requests, check the plugin configuration and logs. The system provides detailed feedback through console messages and player notifications.
