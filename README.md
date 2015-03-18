# demo

Demo vom Brevido DevXChange 03/2015

- Interaktiver Temperaturkonverter
- Interaktive Demo zu [commos.delta](https://github.com/commos/delta)
- Interaktive Demo zu [commos.delta.compscribe](https://github.com/commos/delta.compscribe)

- Interaktives Projekt zum Experimentieren im Quellcode

## Usage

1. Auf System mit installiertem JAVA Clojure Build Tool Leiningen installieren:
[Siehe leiningen.org](http://leiningen.org/#install)

2. Dieses Repository klonen, etwa:

```
git clone https://github.com/lgrapenthin/devxdemo
cd devxdemo
```

3. ClojureScript Build Prozess mit interaktivem REPL starten
```
lein figwheel dev
```
Sobald die Seite in Schritt 7 im Browser offen ist, steht von hier aus auch ein interaktives ClojureScript REPL zur Verfügung.  Zusätzlich werden Änderungen in Dateien mit CLJS Endung automatisch und live nachgezogen.

4. Separate Shell im gleichen Verzeichnis starten

5. Leiningen Clojure REPL starten
```
lein repl
```

6. Am Prompt den Demo Server starten
```clojure
demo.core=> (reset)
```

7. [http://localhost:8080](http://localhost:8080) im Browser öffnen


## License

Copyright © 2015 Leon Grapenthin