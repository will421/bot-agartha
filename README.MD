# Bot Agartha

Discord bot project for the association "Les Arpenteurs d'Agartha"

## Library

* Based on **Kord** et **Kordex** for the discord bot part

## Usage

Need these env variable to be defined :

* TOKEN : discord bot token
* ENV : used to select a guild config file

Guild config file exemple:
name : guildConfig-test.yml

```yaml
---
serverId: "123456789" # Server to connect to
elevatedRole: "12345678910" # Role that can use elevated commands
botAdmin: "123456789101" # User for more experimental/dangerous commands
```