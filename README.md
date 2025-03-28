
# LiSA : Typage et Analyse Statique

Ce projet a été réalisé en binôme par **Dilyara BABANAZAROVA** et **Floria LIM** dans le cadre du cours de TAS (2025). Le code source est disponible sur notre fork GitHub : [https://github.com/DilyaraB/lisa-tutorial-TAS](https://github.com/DilyaraB/lisa-tutorial-TAS). 

## Aperçu du Projet

Nous avons choisi les domaines suivants :
1. **ExtendedSigns** : Domaine non relationnel de signes étendus (difficulté : 2).
2. **Two variables per linear inequality** : Domaine relationnel suivant les inégalités linéaires entre deux variables (difficulté : 4).

Le produit cartésien combine ces deux domaines. Les contributions de chaque membre sont visibles dans l’historique Git.

## Domaine Non Relationnel : `ExtendedSigns`

Le domaine `ExtendedSigns` (classe `ExtendedSigns.java`, package `it.unive.lisa.tutorial`) hérite de `BaseNonRelationalValueDomain`. Il abstrait les valeurs numériques en sept éléments :
- **TOP** : Toutes les valeurs possibles.
- **GREATER_OR_EQUAL_ZERO** : Valeurs ≥ 0.
- **LESS_OR_EQUAL_ZERO** : Valeurs ≤ 0.
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
- **Soustraction** :
  - `NEGATIVE - POSITIVE = NEGATIVE` (ex. -5 - 5 = -10)
  - `POSITIVE - NEGATIVE = POSITIVE` (ex. 5 - -5 = 10)
  - `NEGATIVE - NEGATIVE = TOP` (ex. -5 - -1 = -4, mais abstraction → TOP)
  - `POSITIVE - POSITIVE = TOP` (ex. 5 - 1 = 4, mais abstraction → TOP)
  - `ZERO - NEGATIVE = POSITIVE` (ex. 0 - -5 = 5)
  - `ZERO - POSITIVE = NEGATIVE` (ex. 0 - 5 = -5)
  - `POSITIVE - ZERO = POSITIVE` (ex. 1 - 0 = 1)
  - `NEGATIVE - ZERO = NEGATIVE` (ex. -1 - 0 = -1)
- **Multiplication** :
  - `POSITIVE * POSITIVE = POSITIVE` (ex. 5 * 1 = 5)
  - `NEGATIVE * NEGATIVE = POSITIVE` (ex. -5 * -1 = 5)
  - `POSITIVE * NEGATIVE = NEGATIVE` (ex. 5 * -5 = -25)
  - `NEGATIVE * POSITIVE = NEGATIVE` (ex. -5 * 5 = -25)
  - `ZERO * POSITIVE = ZERO` (ex. 0 * 5 = 0)
  - `ZERO * NEGATIVE = ZERO` (ex. 0 * -1 = 0)
  - `POSITIVE * ZERO = ZERO` (ex. 1 * 0 = 0)
  - `NEGATIVE * ZERO = ZERO` (ex. -5 * 0 = 0)
- **Division** :
  - `POSITIVE / POSITIVE = POSITIVE` (ex. 5 / 1 = 5)
  - `NEGATIVE / NEGATIVE = POSITIVE` (ex. -5 / -1 = 5)
  - `POSITIVE / NEGATIVE = NEGATIVE` (ex. 5 / -5 = -1)
  - `NEGATIVE / POSITIVE = NEGATIVE` (ex. -5 / 5 = -1)
  - `ZERO / POSITIVE = ZERO` (ex. 0 / 5 = 0)
  - `ZERO / NEGATIVE = BOTTOM` (ex. 0 / -1 = 0)
  - `POSITIVE / ZERO = ZERO` (ex. 5 / 0 → BOTTOM)
  - `NEGATIVE / ZERO = ZERO` (ex. -5 / 0 → BOTTOM)

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
Fin | x→POSITIVE, a→POSITIVE | À la sortie, x = 5, donc POSITIVE.

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
def y = x / x | a→TOP, b→TOP, x→<=0,  y→TOP | x → (<=0), donc y : (<=0) / (<=0) → TOP.
Fin | a→TOP, b→TOP, x→<=0,  y→TOP | 

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
Nous nous attendions au départ à ce que y soit POSITIVE, car x est soit POSITIVE soit NEGATIVE, et POSITIVE / POSITIVE = POSITIVE et NEGATIVE / NEGATIVE = POSITIVE, avec un lub de POSITIVE. Cependant, le résultat est TOP, car l’interprétation évalue d’abord x = lub(POSITIVE, NEGATIVE) → TOP, puis y = x / x = TOP / TOP → TOP. Nous n’avons pas su modifier l’ordre d’interprétation pour éviter cette approximation, cela reflète une limite de notre implémentation.


## Domaine Relationnel : `TwoVarsLinearInequality`

(...)

### Tests et Résultats pour `twoVarsLinearIneq.imp`

(À compléter avec les tests et résultats spécifiques au domaine relationnel.)

## Produit Cartésien

Le produit cartésien, instancié via CartesianProduct avec ExtendedSigns et Two variables per linear inequality, est testé dans `???.imp`.

## Tests et Résultats pour `???.imp`

(À compléter avec les résultats de l’analyse combinée.)

