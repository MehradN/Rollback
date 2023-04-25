# Rollback
This Minecraft mod improves the backup system. (Automatic backups, GUI, etc.)
- Rolling back to a backup using in-game GUI
- Creating backups using commands
- Automated backups

![Rollback Screen](https://raw.githubusercontent.com/MehradN/Rollback/master/screenshots/RollbackScreen.png)

### Command
#### /rollback list
Shows a list of the automated backups available for this world.
#### /rollback create
Creates an automated backup
#### /rollback delete ("latest"/"oldest"/<index>)
Deletes the corresponding backup. If "latest" or "oldest" is provided, the latest/oldest backup gets deleted. If an index is provided, the backup with that index gets deleted.
#### /rollback config (<option>) (<value>)
Changes the configuration, similar to the `/gamerule` command.

