# Rollback
This Minecraft mod adds a lot of useful features to the backup system (such as automatic backups and GUI).
- Rolling back to a backup using in-game GUI
- Creating backups using commands
- Automated backups

![Rollback Screen](https://raw.githubusercontent.com/MehradN/Rollback/master/screenshots/RollbackScreen.png)

### Command
#### /rollback list
Shows a list of the automated backups available for this world.
#### /rollback create
Creates an automated backup
#### /rollback delete *value*
Takes a value and deletes the corresponding backup. If the value is "latest" or "oldest" the latest/oldest backup gets deleted. If the value is a number, the backup with that index gets deleted.

