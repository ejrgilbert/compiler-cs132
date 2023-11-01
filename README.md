# CS 132 #

This is a repository that contains my solutions to the HWs for [Jens Palsberg's CS 132 course](https://web.cs.ucla.edu/~palsberg/course/cs132/F14/).

# The Grammar #

## Note ##

- `F` means `EOF`
- `e` means `epsilon`

## LL(1) Grammar
```
// Terminals
concat -> <empty>
binop  -> + | -
incrop -> ++ | --
num    -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

// Non-Terminals (lowest to highest precedence)

// start (lowest precedence)
S -> C F

// concat
C -> E C'
C' -> concat E C'
C' -> e

// binop
E -> T E'
E' -> binop T E'
E' -> e

// pre-increment
T -> incrop T
T -> U

// post-increment
U -> L U'
U' -> incrop U'
U' -> e

// field expression
L -> $L
L -> P

// parentheses (highest precedence)
P -> (C)
P -> num
```

# The FIRST/FOLLOW Sets #

| Non-Terminal  | nullable?  | FIRST          | FOLLOW              |
|---------------|------------|----------------|---------------------|
| S             | no         | incrop $ ( num |                     |
| C             | no         | incrop $ ( num | EOF )               |
| C'            | yes        | concat         | EOF )               |
| E             | no         | incrop $ ( num | concat              |
| E'            | yes        | binop          | concat              |
| T             | no         | incrop $ ( num | concat binop        |
| U             | no         | $ ( num        | concat binop        |
| U'            | yes        | incrop         | concat binop        |
| L             | no         | $ ( num        | incrop concat binop |
| P             | no         | ( num          | incrop concat binop |

# The Predictive Parsing Table #

| Non-Terminal  | concat            | binop            | incrop        | $          | (          | )       | num        | EOF     |
|---------------|-------------------|------------------|---------------|------------|------------|---------|------------|---------|
| S             |                   |                  | S -> C EOF    | S -> C EOF | S -> C EOF |         | S -> C EOF |         |
| C             |                   |                  | C -> E C'     | C -> E C'  | C -> E C'  |         | C -> E C'  |         |
| C'            | C' -> concat E C' |                  |               |            |            | C' -> e |            | C' -> e |
| E             |                   |                  | E -> T E'     | E -> T E'  | E -> T E'  |         | E -> T E'  |         |
| E'            | E' -> e           | E' -> binop T E' |               |            |            |         |            |         |
| T             |                   |                  | T -> incrop U | T -> U     | T -> U     |         | T -> U     |         |
| U             |                   |                  |               | U -> L U'  | U -> L U'  |         | U -> L U'  |         |
| U'            | U' -> e           | U' -> e          | U' -> incrop  |            |            |         |            |         |
| L             |                   |                  |               | L -> $P    | L -> P     |         | L -> P     |         |
| P             |                   |                  |               |            | P -> (C)   |         | P -> num   |         |