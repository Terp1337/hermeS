# Hermes — Sprachgesteuerte Android-App (Jarvis-Design)

## Setup
1. In Android Studio öffnen: `File > Open` → diesen Ordner wählen.
2. Gradle-Sync abwarten (lädt Kotlin/Compose-Abhängigkeiten).
3. Auf echtem Gerät starten (Emulator hat oft kein funktionierendes Mikrofon/Google-Spracherkennung).
4. Beim ersten Tap auf den Ring wird die Mikrofon-Berechtigung angefragt.

## Wie es funktioniert
- **SpeechRecognitionManager.kt** kapselt Androids `SpeechRecognizer` (Google On-Device/Cloud-Hybrid,
  je nach Gerät). Liefert laufenden Text (`partialText`), fertigen Text (`finalText`) und den
  Lautstärkepegel (`rmsLevel`) als Compose-State.
- **JarvisOrb.kt** ist ein reines Canvas-Element: ein sich drehender äußerer Ring reagiert in
  Echtzeit auf `rmsLevel` (wird heller/dicker, je lauter gesprochen wird), plus rotierende
  Tick-Marken für den HUD-Look und einen zweiten, gegenläufigen Ring während des Zuhörens.
- **MainActivity.kt** verdrahtet Permission-Handling, Tap-Geste auf den Ring (Start/Stop),
  und zeigt den erkannten Text darunter an.

## Wo Hermes' eigentliche Logik andockt
In `MainActivity.handleCommand(text: String)` kommt der erkannte Satz an. Dort würdest du
z. B. an dein bestehendes Hermes-Backend (REST/WebSocket) senden oder lokal parsen.

## Naheliegende Erweiterungen
- **Text-to-Speech**: `android.speech.tts.TextToSpeech` für Antworten von Hermes, synchron zur
  Ring-Animation (Ring "spricht" während TTS läuft — gleiche `isListening`-artige State-Variable,
  nur umbenannt in `isSpeaking`).
  Persistenter Listener statt Single-Shot: `SpeechRecognizer` durch einen Wake-Word-Ansatz
  (z. B. Porcupine) ersetzen, damit "Hermes" per Zuruf statt Tap aktiviert wird.
- **Verlauf**: `finalText`-Historie in einer scrollbaren Liste statt nur letzter Satz.
- **Theming**: Farben (`HermesCyan`, `HermesDeepBlue`) sind zentral in `JarvisOrb.kt` definiert —
  leicht an ein anderes Farbschema anpassbar.
