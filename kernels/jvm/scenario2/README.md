# Scénario 2 : Dual-check alarm

> Le buzzer sonne si et seulement si les deux boutons sont pressés en même temps. Relâcher au moins un des deux boutons arrête le son.

## Pourquoi le kernel ne suffit pas

Dans le kernel, une transition sur signal (`SignalTransition`) teste **un seul** capteur avec une seule valeur (`sensor` + `value`).
Le scénario 2 a besoin de **combiner** plusieurs tests :

- `off → on` si `button1` est pressé **ET** `button2` est pressé ;
- `on → off` si `button1` est relâché **OU** `button2` est relâché.

On remplace donc le test « un capteur, une valeur » par une **condition**, simple ou composée.

## Ce qu'on a ajouté au métamodèle

Les conditions suivent le patron **Composite** : une condition est soit une feuille (un test de capteur), soit un nœud qui contient d'autres conditions.

| Élément | Type | Contenu | Rôle |
|---|---|---|---|
| `Condition` | classe abstraite | — | Quelque chose de vrai ou faux à un instant donné |
| `SignalCondition` | hérite de `Condition` | `value : SIGNAL`, référence `sensor` (1) vers `Sensor` | Feuille : « tel capteur vaut telle valeur » |
| `BinaryCondition` | hérite de `Condition` | `operator : LogicalOperator`, contient `conditions` (2) | Combine exactement 2 conditions avec AND ou OR |
| `UnaryCondition` | hérite de `Condition` | contient `condition` (1) | Transforme une seule condition |
| `NOT` | hérite de `UnaryCondition` | — | Inverse la condition contenue |
| `LogicalOperator` | énumération | `AND`, `OR` | Opérateur d'une `BinaryCondition` |

- Le lien vers `Sensor` est une **référence** : la condition désigne un capteur déclaré dans les `bricks` de l'`App`.
- Les liens `conditions` et `condition` sont des **compositions** : les sous-conditions appartiennent à leur parent.
- Une transition à un seul capteur reste simple : sa condition est une unique `SignalCondition`. Le scénario 1 s'écrit donc toujours sans imbrication.

Pour combiner plus de 2 conditions, on imbrique, comme en maths où `a && b && c` vaut `a && (b && c)` :

```
BinaryCondition(AND)
 ├─ b1 = HIGH
 └─ BinaryCondition(AND)
      ├─ b2 = HIGH
      └─ b3 = HIGH
```

## Le modèle du scénario 2

```
App "dualCheckAlarm"
 ├─ bricks
 │   ├─ button1 : Sensor   (pin 8)
 │   ├─ button2 : Sensor   (pin 9)
 │   └─ buzzer  : Actuator (pin 10)
 ├─ initial → off
 └─ states
     ├─ off : actions [buzzer ← LOW]   | transition : button1 = HIGH AND button2 = HIGH → on
     └─ on  : actions [buzzer ← HIGH]  | transition : button1 = LOW  OR  button2 = LOW  → off
```

Chaque transition porte une seule condition, construite ainsi :

```
off → on  :  BinaryCondition(AND)          on → off  :  BinaryCondition(OR)
              ├─ button1 = HIGH                         ├─ button1 = LOW
              └─ button2 = HIGH                         └─ button2 = LOW
```

Les pins sont à adapter au montage réel (connecteurs D8 à D12 du shield).

## Justification des choix

**Binaire + NOT plutôt qu'une condition composée à n éléments (`2..*`).**

| Option | Pour | Contre |
|---|---|---|
| Condition composée (`2..*`) | Arbre plat, simple pour `b1 AND b2 AND b3` | Pas de négation ; s'éloigne d'une grammaire d'expressions classique |
| **Binaire + NOT (retenue)** | Proche de la BNF usuelle (`expr AND expr`), ce qui facilite le DSL externe ; permet la négation | Arbre plus profond pour 3 conditions ou plus |
| Plusieurs transitions par état | Aucune nouvelle classe pour le OR | Ne résout pas le AND |

**Pourquoi des conditions plutôt que des états supplémentaires.** Avec le kernel seul, il faudrait mémoriser les combinaisons de boutons dans des états intermédiaires (`b1 seul`, `b2 seul`, `les deux`…), et leur nombre grandit avec chaque bouton ajouté. Avec une condition composée, la machine garde ses **2 états** (`off`, `on`) : c'est la transition qui teste la combinaison.

**Le NOT.** La condition de retour `on → off` est exactement l'inverse de la condition d'entrée `off → on`. Par la loi de De Morgan :

```
NOT (button1 = HIGH AND button2 = HIGH)   ≡   button1 = LOW OR button2 = LOW
```

Les deux écritures sont équivalentes. Avec `NOT`, l'utilisateur écrit « tout sauf les deux boutons pressés » sans avoir à transformer lui-même la condition, et le lien entre les deux transitions se voit immédiatement. Le `NOT` sert aussi plus tard pour une condition analogique, par exemple `NOT (temp > 57)`.

## Points ouverts

- [ ] **Où vit la condition dans le code ?** Le kernel Java a déjà une `Transition` abstraite avec deux sous-classes, `SignalTransition` (`sensor` + `value`) et `TimeTransition` (délai). Notre diagramme met la condition dans `Transition`. Il faut décider si elle remplace `sensor`/`value` dans `SignalTransition`, ou si elle monte dans `Transition` (et donc concerne aussi `TimeTransition`). Mettre le diagramme UML à jour en conséquence.
- [ ] **`UnaryCondition` / `NOT`.** `NOT` est le seul opérateur unaire : une seule classe `NotCondition` suffirait. Sinon, utiliser un attribut `operator` comme pour `BinaryCondition`. Aujourd'hui les deux mécanismes coexistent, ce qui manque de cohérence.
- [x] **Les deux opérandes.** Un `AND` ou un `OR` a toujours une partie **gauche** et une partie **droite**, et chacune peut elle-même être une expression complète (une `SignalCondition`, un `NOT`, ou une autre `BinaryCondition`). C'est cette récursion qui permet `(b1 AND b2) OR b3`. À refléter dans le diagramme en nommant les deux rôles `left` et `right` au lieu de `conditions (2)`.
- [x] **`transition 0..*` sur `State`.** Un état peut avoir plusieurs transitions sortantes. Le retour `on → off` peut donc s'écrire de deux façons équivalentes : une seule transition `NOT (b1 AND b2)`, ou deux transitions `NOT b1 → off` et `NOT b2 → off`.
- [ ] **Priorité entre transitions.** Avec plusieurs transitions, deux conditions peuvent être vraies en même temps (relâcher les deux boutons rend vraies `NOT b1` et `NOT b2`). Sans conséquence quand elles mènent au même état ; sinon, il faut fixer une règle (par exemple : la première déclarée gagne) et la respecter dans le générateur.

## Reste à faire

- [ ] Ajouter les classes au kernel Java
- [ ] Étendre le générateur `ToWiring` : chaque condition devient une expression booléenne dans le `if` de la transition
- [ ] Écrire `Scenario2.java` et générer `scenario2/scenario2.ino` (même commandes que le scénario 1)
- [ ] Téléverser et vérifier le comportement :

| Action | Résultat attendu |
|---|---|
| Démarrage | Buzzer silencieux |
| Appuyer sur un seul bouton | Toujours silencieux |
| Appuyer sur les deux boutons | Le buzzer sonne |
| Relâcher l'un des deux | Le buzzer s'arrête |
