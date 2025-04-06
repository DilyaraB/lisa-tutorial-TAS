
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
def y = x / x | a→TOP, b→TOP, x→<=0,  BOTTOM | x → (<=0), donc y : (<=0) / (<=0) → BOTTOM.
Fin | a→TOP, b→TOP, x→<=0,  BOTTOM | 

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
Fin | a→TOP, b→TOP, x→TOP,  y->TOP | 

#### Remarque sur branche2 : 
Nous nous attendions au départ à ce que y soit POSITIVE, car x est soit POSITIVE soit NEGATIVE, et POSITIVE / POSITIVE = POSITIVE et NEGATIVE / NEGATIVE = POSITIVE, avec un lub de POSITIVE. Cependant, le résultat est TOP, car l’interprétation évalue d’abord x = lub(POSITIVE, NEGATIVE) → TOP, puis y = x / x = TOP / TOP → TOP. Nous n’avons pas su modifier l’ordre d’interprétation pour éviter cette approximation, cela reflète une limite dans notre implémentation.


## Domaine Relationnel : `TwoVarsLinearInequality`

(...)

### Tests et Résultats pour `twoVarsLinearIneq.imp`

(À compléter avec les tests et résultats spécifiques au domaine relationnel.)

## Produit Cartésien : `ExtendedSignsTVPIProductDomain`

Le produit cartésien, implémenté dans la classe `ExtendedSignsTVPIProductDomain` (package `it.unive.lisa.tutorial`), combine le domaine non relationnel `ExtendedSigns` et le domaine relationnel `TwoVarsLinearInequality` via l’héritage de `ValueCartesianProduct`. La méthode `reduce()` tente de maintenir une cohérence entre les signes et les contraintes linéaires, mais hérite des approximations de `TwoVarsLinearInequality`.

### Fonctionnement
- **Construction** : Le domaine est initialisé avec un `ValueEnvironment<ExtendedSigns>` (gestion des signes) et un `TwoVarsLinearInequality` (contraintes linéaires).
- **Réduction** : La méthode `reduce()` effectue une passe unique pour raffiner les deux domaines :
  1. **Raffinement de `TwoVarsLinearInequality`** : Ajoute des contraintes basées sur les signes (ex. `x >= 0` si `x: GREATER_OR_EQUAL_ZERO`).
  2. **Raffinement de `ExtendedSigns`** : Calcule les bornes les plus strictes à partir des contraintes TVPI et ajuste les signes (ex. `x <= 0` et `x >= 0` → `x: ZERO`).
  3. **Raffinement relationnel** : Exploite les inégalités à deux variables (ex. `x - y <= 0` → si `x: POSITIVE`, alors `y: GREATER_OR_EQUAL_ZERO`).
- **Opérations sémantiques** : Les méthodes `assign`, `assume`, et `forgetIdentifier` appliquent les transformations sur chaque domaine, suivies d’un appel à `reduce()` pour maintenir la cohérence.

---

### Tests et Résultats pour `extendedSignsTVPI.imp`

Le fichier `extendedSignsTVPI.imp` teste le produit cartésien avec trois fonctions : `test1`, `test2`, et `test3`. Voici les résultats détaillés :

#### `test1(x)`
```java
test1(x) {
    def y = x + 1;
    def z = y + 2;
    if (x <= 0) {       
        def w = z + y;
    } else {
        def w = z - y;
    }
}
```

#### `test2()`
```java
test2() {
    def x = 0;
    def y = x + 1;
    def z = -1;
    if (y >= 1) {
        z = 0;
    }
}
```

#### `test3()`
```java
test3() {
    def y = 0;
    def z = 0;
    while (y < 10) {
        y = y + 1;
    }
    z = y + 1;
}
```

### Résultats des tests
Les résultats des tests dans `extendedSignsTVPI.imp` révèlent les comportements suivants :  
- **test1(x)** : Les relations initiales, telles que `z - x ≤ 3`, sont correctement déduites. Toutefois, la fusion des branches via `lub` intègre des contraintes incohérentes (par exemple, `y ≤ 1` et `y ≥ 2`), en raison de l’absence d’une vérification de satisfiabilité robuste dans `TwoVarsLinearInequality`.  
- **test2()** : L’analyse est précise pour `x: 0` et `y: +`, mais la valeur finale de `z` est surapproximée à `<=0` au lieu de `0`. Cela découle d’une gestion insuffisante des constantes dans `assume` (par exemple, `y >= 1` ne renforce pas `y = 1`), permettant la persistance de contraintes erronées comme `y ≤ 0`.  
- **test3()** : La boucle `while (y < 10)` est mal interprétée, la condition `y < 10` n’étant pas traduite en une contrainte telle que `y ≤ 9`. De plus, le `widening` inefficace conduit à une accumulation de contraintes incorrectes (par exemple, `y ≥ 20`), au lieu de stabiliser la borne à `y ≤ 10`.

## Améliorations et limites

- **Contributions positives** :  
  - L’abstraction des signes par `ExtendedSigns` est correctement intégrée.  
  - Certaines relations linéaires sont capturées avec succès dans des cas simples.  
- **Limites** :  
  - Les approximations de `TwoVarsLinearInequality`, notamment l’absence de vérification de satisfiabilité, la gestion lacunaire des constantes, et un `widening` inadapté, entraînent des surapproximations significatives.  
  - Ces limitations se répercutent sur les analyses impliquant des fusions ou des boucles, réduisant la précision globale.
  
## Conclusion

Ce projet s’est révélé exigeant sur le plan technique et conceptuel. L’objectif d’implémenter une version précise de `TwoVarsLinearInequality` n’a pu être pleinement atteint en raison de sa complexité inhérente. Les tentatives d’amélioration ont souvent conduit à des résultats partiellement fonctionnels, mais introduisaient des incohérences ailleurs, rendant difficile l’identification précise des sources d’erreur. 

Au final, l’analyse produite constitue une surapproximation opérationnelle, bien que perfectible. Malgré ces obstacles, cette expérience a offert une opportunité précieuse d’approfondir notre compréhension de l’analyse statique et des interactions entre domaines dans un cadre comme LiSA.
