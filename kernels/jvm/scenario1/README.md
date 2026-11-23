# Scénario 1 : Very simple alarm

> Appuyer sur le bouton allume la LED et le buzzer. Relâcher le bouton les éteint.

## Ce qu'on a fait

On a décrit le scénario directement avec les classes du **kernel ArduinoML** (Java), sans DSL pour l'instant.
Le fichier `kernels/jvm/src/main/java/io/github/mosser/arduinoml/kernel/samples/Scenario1.java` construit le modèle suivant :

```
App "alarme"
 ├─ bricks
 │   ├─ button : Sensor   (pin 9)
 │   ├─ led    : Actuator (pin 12)
 │   └─ buzzer : Actuator (pin 11)
 ├─ initial → off
 └─ states
     ├─ off : actions [led ← LOW,  buzzer ← LOW]   | transition : button = HIGH → on
     └─ on  : actions [led ← HIGH, buzzer ← HIGH]  | transition : button = LOW  → off
```

Le générateur du kernel (`ToWiring`, un visitor) parcourt ce modèle et produit le code Arduino `scenario1.ino`.

```
Scenario1.java (modèle)  →  ToWiring (générateur)  →  scenario1.ino (code Arduino)
```

## Générer le code Arduino

Depuis le dossier `kernels/jvm`, dans un terminal PowerShell :

```powershell
# 1. Compiler tout le kernel (pas seulement Scenario1.java)
javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName

# 2. Générer le code Arduino dans scenario1/scenario1.ino
java -cp out io.github.mosser.arduinoml.kernel.samples.Scenario1 | Out-File -Encoding ascii scenario1\scenario1.ino
```

Sous Git Bash / Linux / macOS :

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out io.github.mosser.arduinoml.kernel.samples.Scenario1 > scenario1/scenario1.ino
```

⚠️ Pièges rencontrés :
- **Ne pas utiliser le bouton Code Runner (▷) de VS Code** : il compile `Scenario1.java` seul et ne trouve pas les classes du kernel (`cannot find symbol`).
- **Ne pas utiliser `>` sous PowerShell** : il écrit en UTF-16 et l'IDE Arduino affiche l'erreur `stray '\377' in program`. Utiliser `Out-File -Encoding ascii`.

## Téléverser sur la carte

1. Brancher les composants sur le shield :

   | Composant | Broche |
   |---|---|
   | Bouton | D9 |
   | LED | D12 |
   | Buzzer | D11 |

2. Ouvrir `scenario1/scenario1.ino` dans l'**IDE Arduino**.
3. Brancher la carte en USB, puis **Tools → Board → Arduino Uno** et **Tools → Port → COMx**.
4. Cliquer sur **✓ Verify**, puis sur **→ Upload**.

## Vérifier le comportement

| Action | Résultat attendu |
|---|---|
| Démarrage | LED éteinte, buzzer silencieux |
| Appuyer et maintenir le bouton | LED allumée et buzzer qui sonne |
| Relâcher le bouton | Tout s'éteint |
| Recommencer | Même comportement |