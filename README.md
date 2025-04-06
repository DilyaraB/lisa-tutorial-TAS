
# LiSA : Typage et Analyse Statique

Ce projet a été réalisé en binôme par **Dilyara BABANAZAROVA** et **Floria LIM** dans le cadre du cours de TAS (2025). Le code source est disponible sur notre fork GitHub : [https://github.com/DilyaraB/lisa-tutorial-TAS](https://github.com/DilyaraB/lisa-tutorial-TAS). 

## Aperçu du Projet

Dans ce projet, nous avons implémenté et combiné deux domaines d’analyse statique dans LiSA :

- **ExtendedSigns** : Domaine non relationnel de signes étendus (difficulté : 2).
- **TwoVarsLinearInequality** : Domaine relationnel d’inégalités linéaires entre deux variables (difficulté : 4).
- **ExtendedSignsTVPIProductDomain** : Produit cartésien des deux domaines pour une analyse hybride.

## Domaine Non Relationnel : `ExtendedSigns`

Le domaine `ExtendedSigns` (classe `ExtendedSigns.java`, package `it.unive.lisa.tutorial`) hérite de `BaseNonRelationalValueDomain`. Il abstrait les valeurs numériques en sept éléments :
- **TOP** : Toutes les valeurs possibles.
- **GREATER_OR_EQUAL_ZERO** : Valeurs >= 0.
- **LESS_OR_EQUAL_ZERO** : Valeurs <= 0.
- **ZERO** : Valeur = 0.
- **POSITIVE** : Valeurs > 0.
- **NEGATIVE** : Valeurs < 0.
- **BOTTOM** : Absence de valeur ou erreur (ex. division par zéro).

### Fonctionnalités
- **Opérations** : Addition, soustraction, multiplication, division, négation unitaire.
- **Comparaisons** : `==`, `>`, `<`, `>=`, `<=`, `!=`.
- **Constantes** : Évaluation des nombres (ex. `5 → POSITIVE`).
- **Boucles et conditions** : Gestion des structures de contrôle via satisfiabilité.

### Tests et Résultats pour `extendedSigns.imp`

Le programme IMP `extendedSigns.imp` teste les opérations et structures de contrôle. Voici les résultats détaillés :

#### Opérations Arithmétiques
- **Addition** :
  - `NEGATIVE + NEGATIVE = NEGATIVE` (ex. -5 + -1 = -6)
  - `POSITIVE + POSITIVE = POSITIVE` (ex. 5 + 1 = 6)
  - `NEGATIVE + POSITIVE = TOP` (ex. -5 + 5 = 0, mais abstraction → TOP)
  - `POSITIVE + NEGATIVE = TOP` (ex. 5 + -1 = 4, mais abstraction → TOP)
  - `ZERO + POSITIVE = POSITIVE` (ex. 0 + 5 = 5)
  - `ZERO + NEGATIVE = NEGATIVE` (ex. 0 + -5 = -5)
  - `NEGATIVE + ZERO = NEGATIVE` (ex. -1 + 0 = -1)
  - `POSITIVE + ZERO = POSITIVE` (ex. 1 + 0 = 1)
  - `>=ZERO + <=ZERO = TOP`
  - `<=ZERO + >=ZERO = TOP`
- **Soustraction** :
  - `NEGATIVE - POSITIVE = NEGATIVE` (ex. -5 - 5 = -10)
  - `POSITIVE - NEGATIVE = POSITIVE` (ex. 5 - -5 = 10)
  - `NEGATIVE - NEGATIVE = TOP` (ex. -5 - -1 = -4, mais abstraction → TOP)
  - `POSITIVE - POSITIVE = TOP` (ex. 5 - 1 = 4, mais abstraction → TOP)
  - `ZERO - NEGATIVE = POSITIVE` (ex. 0 - -5 = 5)
  - `ZERO - POSITIVE = NEGATIVE` (ex. 0 - 5 = -5)
  - `POSITIVE - ZERO = POSITIVE` (ex. 1 - 0 = 1)
  - `NEGATIVE - ZERO = NEGATIVE` (ex. -1 - 0 = -1)
  - `>=ZERO - <=ZERO = >=ZERO`
  - `<=ZERO - >=ZERO = <=ZERO`
- **Multiplication** :
  - `POSITIVE * POSITIVE = POSITIVE` (ex. 5 * 1 = 5)
  - `NEGATIVE * NEGATIVE = POSITIVE` (ex. -5 * -1 = 5)
  - `POSITIVE * NEGATIVE = NEGATIVE` (ex. 5 * -5 = -25)
  - `NEGATIVE * POSITIVE = NEGATIVE` (ex. -5 * 5 = -25)
  - `ZERO * POSITIVE = ZERO` (ex. 0 * 5 = 0)
  - `ZERO * NEGATIVE = ZERO` (ex. 0 * -1 = 0)
  - `POSITIVE * ZERO = ZERO` (ex. 1 * 0 = 0)
  - `NEGATIVE * ZERO = ZERO` (ex. -5 * 0 = 0)
  - `>=ZERO * <=ZERO = <=ZERO`
- **Division** :
  - `POSITIVE / POSITIVE = POSITIVE` (ex. 5 / 1 = 5)
  - `NEGATIVE / NEGATIVE = POSITIVE` (ex. -5 / -1 = 5)
  - `POSITIVE / NEGATIVE = NEGATIVE` (ex. 5 / -5 = -1)
  - `NEGATIVE / POSITIVE = NEGATIVE` (ex. -5 / 5 = -1)
  - `ZERO / POSITIVE = ZERO` (ex. 0 / 5 = 0)
  - `ZERO / NEGATIVE = BOTTOM` (ex. 0 / -1 = 0)
  - `POSITIVE / ZERO = ZERO` (ex. 5 / 0 → BOTTOM)
  - `NEGATIVE / ZERO = ZERO` (ex. -5 / 0 → BOTTOM)
  - `>=ZERO / <=ZERO = BOTTOM`

#### Structures de Contrôle
- **Boucle (`loop`)** :
```java
def x = 0;
def a = 5;
while (x < a) {
    x = x + 1;
}
```

Ligne | Résultat | Explication
--- | --- | ---
def x = 0 | x→ZERO | 
def a = 5 | x→ZERO, a→POSITIVE |
while (x < a) | x→TOP, a→POSITIVE | Au début on a while(ZERO < POSITIVE), mais après on a while(POSITIVE < POSITIVE) → TOP (perte de précision).
x = x + 1 | x→TOP, a→POSITIVE | x reste à TOP car on avait x→TOP avant.
Fin | x→POSITIVE, a→POSITIVE | À la sortie, x >= a c'est-à-dire x >= POSITIVE, donc POSITIVE.

- **Conditionnel (conditional)** :
```java
def i = -1; 
def j = 1;  
if (i > 1) {  
    i = 1;   
    j = 2;    
}
if (i < 1) {  
    i = i + 1; 
    j = j + 2; 
}
```

Ligne | Résultat | Explication
--- | --- | ---
def i = -1 | i→NEGATIVE | 
def j = 1 | i→NEGATIVE, j→POSITIVE | 
if (i > 1) | i→NEGATIVE, j→POSITIVE | NEGATIVE > POSITIVE → NOT_SATISFIED, branche non prise.
i = 1 | BOTTOM | Non exécuté.
j = 2 | BOTTOM | Non exécuté.
if (i < 1) | i→NEGATIVE, j→POSITIVE | NEGATIVE < POSITIVE → SATISFIED, branche prise.
i = i + 1 | i→TOP, j→POSITIVE | i = NEGATIVE + POSITIVE → TOP (signe ambigu).
j = j + 2 | i→TOP, j→POSITIVE | POSITIVE + POSITIVE → POSITIVE.
Fin | i→TOP, j→POSITIVE | 

- **Branche 1 (branche1)** :
```java
def x = 0;  
if (a > b)  
    x = x * 7;  
else
    x = -7;  
def y = x / x;  
```

Ligne | Résultat | Explication
--- | --- | ---
def x = 0 | a→TOP, b→TOP, x→ZERO | a et b sont des paramètres.
if (a > b) | a→TOP, b→TOP, x→ZERO | Satisfaction dépendant des valeurs de a et b.
x = x * 7 | a→TOP, b→TOP, x→ZERO | ZERO * POSITIVE → ZERO.
x = -7 | a→TOP, b→TOP, x→NEGATIVE | Assignation à -7 → NEGATIVE.
def y = x / x | a→TOP, b→TOP, x→<=0, y→BOTTOM | x → (<=0), donc y : (<=0) / (<=0) → BOTTOM.
Fin | a→TOP, b→TOP, x→<=0, y→BOTTOM | 

- **Branche 2 (branche2)** :
```java
def x = 7;  
if (a > b)
    x = x * 7;  
else
    x = -7;
def y = x / x;
```

Ligne | Résultat | Explication
--- | --- | ---
def x = 7 | a→TOP, b→TOP, x→POSITIVE | a et b sont des paramètres.
if (a > b) | a→TOP, b→TOP, x→POSITIVE | Satisfaction dépendant des valeurs de a et b.
x = x * 7 | a→TOP, b→TOP, x→POSITIVE | POSITIVE * POSITIVE → POSITIVE.
x = -7 | a→TOP, b→TOP, x→NEGATIVE | Assignation à -7 → NEGATIVE.
def y = x / x | a→TOP, b→TOP, x→TOP, y→TOP | x → POSITIVE ou NEGATIVE → TOP, donc y = TOP / TOP → TOP.
Fin | a→TOP, b→TOP, x→TOP, y→TOP | 

#### Remarque sur branche2 : 
Nous nous attendions au départ à ce que y soit POSITIVE, car x est soit POSITIVE soit NEGATIVE, et POSITIVE / POSITIVE = POSITIVE et NEGATIVE / NEGATIVE = POSITIVE, avec un lub de POSITIVE. Cependant, le résultat est TOP, car l’interprétation évalue d’abord x = lub(POSITIVE, NEGATIVE) → TOP, puis y = x / x = TOP / TOP → TOP. Nous n’avons pas su modifier l’ordre d’interprétation pour éviter cette approximation, cela reflète une limite dans notre implémentation.


## Domaine Relationnel : `TwoVarsLinearInequality`

Le domaine `TwoVarsLinearInequality` (classe `TwoVarsLinearInequality.java`, package `it.unive.lisa.tutorial`) est une implémentation simplifiée du domaine relationnel **TVPI** (*Two Variables Per Inequality*). Il permet de représenter des relations de la forme :

    a * x + b * y <= c

où `x` et `y` sont des identifiants, `a`, `b` et `c` sont des constantes entières. Il capture ainsi des **relations linéaires entre deux variables** à chaque instant du programme.

Ce domaine maintient un ensemble de contraintes linéaires, fermé par transitivité, pour capturer les relations entre paires de variables à chaque point du programme.

### Fonctionnement

### Représentation
- Une contrainte est un objet `TwoVarsInequality` avec les champs `a`, `x`, `b`, `y`, `c`.
- Exemple : `x - y <= 2` devient `1*x + (-1)*y <= 2` (soit `a=1`, `x=x`, `b=-1`, `y=y`, `c=2`).
- L’état du domaine est :
  - Un ensemble de telles contraintes (état normal).
  - `TOP` : Toutes les valeurs possibles (ensemble vide de contraintes avec `isTop = true`).
  - `BOTTOM` : État insatisfiable (singleton `{0 <= -1}`).

### Opérations principales
1. **Assignations (`assign`)**
  - `x = y` : Ajoute `{x - y <= 0, y - x <= 0}`.
  - `x = y + c` : Ajoute `{x - y <= c, y - x <= -c}`.
  - Oublie les contraintes précédentes sur `x` via `project` avant d’ajouter les nouvelles.
2. **Conditions (`assume`)**
  - `x <= y` : Ajoute `{x - y <= 0}`.
  - `x <= y + c` : Ajoute `{x - y <= c}`.
3. **Fermeture transitive (`computeClosure`)**
  - Si `x - y <= c1` et `y - z <= c2`, alors `x - z <= c1 + c2`.
  - Vérifie la satisfiabilité et retourne `BOTTOM` si une contradiction est détectée.
4. **Projection (`project`)**
  - Supprime toutes les contraintes impliquant un identifiant donné.
5. **Satisfiabilité (`checkSatisfiability`)**
  - Détecte les contradictions (ex. `c - a <= 1` et `-c + a <= -2` → `0 <= -1`).
6. **Élimination des redondances (`eliminateRedundancies`)**
  - Garde la contrainte la plus restrictive pour chaque paire `(x, y, a, b)`.

### Tests et Résultats pour `twoVarsLinearIneq.imp`

Nous avons implémenté plusieurs tests pour évaluer notre domaine relationnel. Voici les résultats détaillés :

#### Test 1 : `basic(x)`

```java
basic(x) {
  def y = x + 1;
  def z = y + 2;
  if (x <= z) {
    def w = z;
  }
}
```

Ligne | Contraintes ajoutées                                                                                                                                   | Explication
--- |--------------------------------------------------------------------------------------------------------------------------------------------------------| ---
def y = x + 1 | [1\*y - 1\*x <= 1, -1\*y + 1\*x <= -1]                                                                                                                     | Traduit `y = x + 1`.
def z = y + 2 | [1\*y - 1\*x <= 1, -1\*y + 1\*x <= -1, 1\*z - 1\*y <= 2, -1\*z + 1\*y <= -2]                                                                              | Ajoute `z = y + 2`.
Fermeture | [1\*z - 1\*y <= 2, 1\*z - 1\*x <= 3, 1\*y - 1\*x <= 1, -1\*y + 1\*x <= -1, -1\*z + 1\*y <= -2, -1\*z + 1\*x <= -3]                                                 | Transitivité : `1*y - 1*x <= 1 et 1*z - 1*y <= 2 → 1*z - 1*x <= 3`, etc..
if (x <= z) | [..., 1\*x - 1\*z <= 0]                                                                                                                                  | Ajoute la condition `x <= z`.
Fermeture | [1\*z - 1\*y <= 2, 1\*z - 1\*x <= 3, 1\*x - 1\*z <= 0, 1\*y - 1\*z <= 1, 1\*y - 1\*x <= 1, 1\*x - 1\*y <= 2, -1\*y + 1\*x <= -1, -1\*z + 1\*y <= -2, -1\*z + 1\*x <= -3] | État après la condition, avec nouvelles déductions.
def w = z | [..., 1\*w - 1\*z <= 0, -1\*w + 1\*z <= 0]                                                                                                                 | Traduit `w = z`.
Fermeture | [..., 1\*w - 1\*x <= 3, 1\*w - 1\*y <= 2, -1\*w + 1\*z <= 0, -1\*w + 1\*y <= -2, -1\*w + 1\*x <= -3, -1\*y + 1\*x <= -1, -1\*z + 1\*y <= -2, -1\*z + 1\*x <= -3]       | Transitivité 
Fin  | [1\*z - 1\*y <= 2, 1\*z - 1\*x <= 3, 1\*x - 1\*z <= 0, 1\*y - 1\*z <= 1, 1\*y - 1\*x <= 1, 1\*x - 1\*y <= 2, -1\*y + 1\*x <= -1, -1\*z + 1\*y <= -2, -1\*z + 1\*x <= -3] | Fusion(lub) et oubli de `w`.
---

#### Test 2 : `complex(a)`

```java
complex(a) {
  def b = a + 1;
  def c = b;
  if (b <= c) {
    c = a + 2;
  }
  def d = c + 1;
}
```

Ligne | Contraintes ajoutées | Explication
--- | --- | ---
def b = a + 1 |	[1\*b - 1\*a <= 1, -1\*b + 1\*a <= -1] | Traduit `b = a + 1`.
def c = b | [1\*c - 1\*b <= 0, 1\*c - 1\*a <= 1, 1\*b - 1\*a <= 1, -1\*c + 1\*b <= 0, -1\*b + 1\*a <= -1, -1\*c + 1\*a <= -1] | Ajoute c = b. Fermeture : `1*c - 1*b <= 0` et `1*b - 1*a <= 1` → `1*c - 1*a <= 1`, etc.
if (b <= c) | [1\*b - 1\*c <= 0, 1\*c - 1\*b <= 0, 1\*c - 1\*a <= 1, 1\*b - 1\*a <= 1, -1\*c + 1\*b <= 0, -1\*b + 1\*a <= -1, -1\*c + 1\*a <= -1] | Ajoute `b <= c`, cohérent avec `c = b`.
Branche then : c = a + 2 | [1\*b - 1\*a <= 1, 1\*c - 1\*a <= 2, -1\*b + 1\*a <= -1, -1\*c + 1\*a <= -2] | project(c) oublie les contraintes sur `c`, puis ajoute `c = a + 2`.
Branche else | [1\*b - 1\*c <= 0, 1\*c - 1\*b <= 0, 1\*c - 1\*a <= 1, 1\*b - 1\*a <= 1, -1\*c + 1\*b <= 0, -1\*b + 1\*a <= -1, -1\*c + 1\*a <= -1] | État inchangé (avant if).
Fusion (lub) | BOTTOM |	Union inclut `1*c - 1*a <= 1` (de else) et `-1*c + 1*a <= -2` (de then). Contradiction : `0 <= -1`.
def d = c + 1 | BOTTOM | L’état reste BOTTOM.
---

#### Limites actuelles
- **Opérations complexes** : Se limite aux assignations additives et comparaisons : ComparisonLe, ComparisonGe. Pas de gestion de loop.
- **Tests** : Tous les cas possibles du domaine n'ont pas été testés.

## Produit Cartésien : `ExtendedSignsTVPIProductDomain`

La classe `ExtendedSignsTVPIProductDomain` (package `it.unive.lisa.tutorial`) implémente un produit cartésien entre `ExtendedSigns` et `TwoVarsLinearInequality` via `ValueCartesianProduct`. La méthode `reduce()` assure une cohérence bidirectionnelle : elle traduit les signes en contraintes linéaires et ajuste les signes à partir des contraintes TVPI. Cependant, l’absence d’une méthode `lub` explicite limite la précision lors des fusions de branches.

### Fonctionnement
- **Construction** : Le domaine est initialisé avec un `ValueEnvironment<ExtendedSigns>` (gestion des signes des variables) et un `TwoVarsLinearInequality` (ensemble de contraintes linéaires).
- **Réduction** : La méthode `reduce()` effectue une passe unique pour maintenir la cohérence entre les deux domaines :
  1. **Raffinement de `TwoVarsLinearInequality`** : Ajoute des contraintes unaires basées sur les signes actuels (ex. `x: ZERO` → `1*x <= 0` et `-1*x <= 0`, `x: POSITIVE` → `-1*x <= -1`).
  2. **Raffinement de `ExtendedSigns`** : Analyse les contraintes unaires dans `TVPI` pour ajuster les signes (ex. si `1*x <= 0` et `-1*x <= 0`, alors `x: ZERO` ; si `-1*x <= -1` et pas de borne supérieure stricte, alors `x: POSITIVE`).
- **Opérations sémantiques** : Les méthodes `assign`, `assume`, et `forgetIdentifier` délèguent les transformations aux domaines sous-jacents (`ExtendedSigns` et `TwoVarsLinearInequality`), suivies d’un appel à `reduce()` pour synchroniser les résultats.


---

### Tests et Résultats pour `extendedSignsTVPI.imp`

Le fichier `extendedSignsTVPI.imp` teste le produit cartésien avec trois fonctions : `test1`, `test2`, et `test3`. Voici les résultats détaillés :

#### Test 1 : `test1()`

```java
test1(x) {
    def y = x + 1;
    def z = 0;
    if (y >= 1) {
    	z = 1;
    }
}
```

Ligne | Résultat | Explication
--- | --- | ---
def y = x + 1 | ExtendedSigns: { x: TOP, y: TOP }, TVPI: [1\*y - 1\*x <= 1, -1\*y + 1\*x <= -1] | `y = x + 1` → contraintes linéaires, `x` et `y` restent indéfinis.
def z = 0 | ExtendedSigns: { x: TOP, y: TOP, z: 0 }, TVPI: [-1\*z <= 0, 1\*y - 1\*x <= 1, 1\*z <= 0, -1\*y + 1\*x <= -1] | `z = 0` → `z: ZERO`, ajout de `1*z <= 0` et `-1*z <= 0`.
if (y >= 1) | ExtendedSigns: { x: TOP, y: TOP, z: 0 }, TVPI: [-1\*z <= 0, 1\*y - 1\*x <= 1, 1\*z <= 0, -1\*y + 1\*x <= -1] | Condition `y >= 1` appliquée, mais `y: TOP` reste vague avant la branche.
Branche then : z = 1 | ExtendedSigns: { x: TOP, y: +, z: + }, TVPI: [-1\*z <= -1, -1\*y <= -1, 1\*y - 1\*x <= 1, -1\*y + 1\*x <= -1] | `y >= 1` → `y: POSITIVE`, `z = 1` → `z: POSITIVE`, contraintes ajustées par `reduce()`.
Fin | ExtendedSigns: { x: TOP, y: TOP, z: >=0 }, TVPI: [-1\*z <= -1, -1\*y <= -1, 1\*y - 1\*x <= 1, 1\*z <= 0, 1\*y <= 0, -1\*y + 1\*x <= -1] | Fusion entre `z: ZERO` (avant `if`) et `z: POSITIVE` (dans `if`) → `z: >=0`, `y` retombe à TOP.

#### Test 2 : `test2()`

```java
test2() {
    def x = 0;
    def y = x - 1;
    def z = y + x; 
    x = y + z;
}
```

Ligne | Résultat | Explication
--- | --- | ---
def x = 0 | ExtendedSigns: { x: 0 }, TVPI: [-1\*x <= 0, 1\*x <= 0] | `x = 0` → `x: ZERO`.
def y = x - 1 | ExtendedSigns: { x: 0, y: - }, TVPI: [-1\*x <= 0, 1\*y <= -1, 1\*x <= 0] | `y = -1` → `y: -`, `reduce()` déduit `y <= -1`.
def z = y + x | ExtendedSigns: { x: 0, y: -, z: - }, TVPI: [-1\*x <= 0, 1\*z <= -1, 1\*y <= -1, 1\*x <= 0] | `z = -1 + 0` → `z: -`, `1*z <= -1`.
x = y + z | ExtendedSigns: { x: -, y: -, z: - }, TVPI: [1\*z <= -1, 1\*y <= -1, 1\*x <= -1] | `x = -1 + -1` → `x: -`, contraintes cohérentes.
Fin | ExtendedSigns: { x: -, y: -, z: - }, TVPI: [1\*z <= -1, 1\*y <= -1, 1\*x <= -1] | État final précis, pas de fusion.

### Commentaires
Les résultats des tests montrent une bonne synchronisation entre `ExtendedSigns` et `TwoVarsLinearInequality` dans les cas linéaires simples, où les signes et contraintes restent cohérents sans branchement. Cependant, la méthode `reduce()` se concentre sur les contraintes unaires (ex. `x <= c`) uniquement, ce qui limite son efficacité pour utiliser des relations entre deux variables, comme `y - x <= 1`, afin d’affiner les signes.

### Limites
- Les boucles `while` ne sont pas gérées, car `TwoVarsLinearInequality` n’a pas été conçu pour analyser les programmes itératifs.
- De même, les opérations complexes (ex. multiplication, division) ne sont pas prises en charge dans les assignations, car `TwoVarsLinearInequality` se limite aux transformations additives.

## Conclusion

Ce projet a été un défi technique significatif. L’objectif d’implémenter une version précise de `TwoVarsLinearInequality` n’a pu être pleinement atteint en raison de sa complexité inhérente. Bien que nous ayons implémenté avec succès `ExtendedSigns`, offrant une abstraction robuste des signes, et implémenté `TwoVarsLinearInequality` avec une fermeture transitive et une vérification de satisfiabilité, la précision globale reste limitée. Le produit cartésien `ExtendedSignsTVPIProductDomain` combine efficacement les deux domaines dans des cas simples, et constitue une avancée notable. 

Au final, l’analyse produite constitue une surapproximation opérationnelle mais insuffisante. Les tentatives d’amélioration ont souvent conduit à des résultats partiellement fonctionnels, mais introduisaient des incohérences ailleurs, rendant difficile l’identification précise des sources d’erreur. 

Cette expérience a offert une opportunité précieuse d’approfondir notre compréhension de l’analyse statique et des interactions entre domaines dans un cadre comme LiSA, posant les bases pour des améliorations futures.