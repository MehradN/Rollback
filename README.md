# Rollback
This Minecraft mod will take automatic backups of your single-player world and will allow you to rollback to them using the GUI. <br>
**Currently at pre-release state**

### Screenshots
![Rollback Screen](https://raw.githubusercontent.com/MehradN/Rollback/master/screenshots/RollbackScreen.png)
![Config Screen (Powered by Midnightlib)](https://raw.githubusercontent.com/MehradN/Rollback/master/screenshots/ConfigScreen.png)

### Automated Backup Frequency: What's the difference between "X Per Day" and "X Minutes"?
The "X Per Day" is based on the daylight cycle. For example, the "2 Per Day" option will take backups only at 6:00 and 18:00 (in-game time). However, this means that a backup can be skipped by beds and commands. The mod has a cap on the minimum amount of real-time that should've passed since the last backup (which is 10/X minutes) so useless backups won't be created.

The "X Minutes" however is a strict timing. It will take a backup exactly every X minutes, regardless of the daylight cycle. This option is better for the worlds that have the daylight cycle disabled or if you are using a mod that alters the daylight cycle or if you just want reliable and strict timing.

### Command
#### /rollback list
Will show a simple list of all the backups available for this world.
#### /rollback backup now
Creates an automated backup
#### /rollback backup delete
Takes an index and deletes that backup. This version of this command requires a restart every time you change the "Maximum Backups Per World" option, to work properly. <br>
It can also take the "latest" and "oldest" which will delete the latest/oldest backup.
