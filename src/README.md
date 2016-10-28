(c)2016, Tobias Hotz
BackupBasic, Version 1.3.6

Changelog:
	1.3.6: JUL implementiert, Logging to File, JOptionPane Fehlermeldungen mit Fehlersymbol versehen.
	1.3.5: CheckSum auch über args deaktivierbar
	1.3.4: ThreadedBackup Falls bei einem Abbruch der Timeout erreicht wurde, wird eine Infodatei erstellt.
		   ArgParser: Auch \ filtern, falls der Benutzer dies eingibt
	1.3.3: Logging: Notice-Log hinzugefügt, Log-Refactor, kleine Änderungen CalcCheckSum
	1.3.2: Prüfsummenüberprüfung kann deaktiviert werden(GUI only)
	1.3.1: Nur wenn Dateianzahl gleich Prüfsumme über alte Dateien ziehen
	1.3: Verwendung auf Systemen ohne GUI wieder möglich, viele interne Verbesserungen
	1.2.3: Shutdown-Hooks, um bei Beendigungssignal zu versuchen, die letzte Datei zu kopieren.
	1.2.2: Backup von anderem Thread starten, um abbrechen möglich zu machen
	1.2.1: Simple "Arbeite" GUI hinzugefügt
	1.2: GUI hinzugefügt
	1.1.1: Prüfsummenberchnung auch für Unterordner
	1.1: Support für Kopieren von Unterordner hinzugefügt
	1.0.1: Komandozeilenargumente hinzugefügt
	1.0: Erste Herausgabe
	
Dieses Programm ist primär für das Backup von Savegames gedacht.
Dazu können die Parameter Quelle und Ziel angegeben werden.

Teile der Klasse ClalcMD5 sind von Stuart Rossiter. Für weitere Informationen bitte den Quelltext anschauem.

Dieses Programm benutzt die Bibliothek "commons-codec-1.10.jar", Lizensiert unter der Apache License(2.0)
Weitere Informationen finden Sie unter "LICDESE commons-codec.txt"