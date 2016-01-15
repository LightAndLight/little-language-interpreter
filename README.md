# little-language-interpreter
An interpreter for a tiny subset of arithmetic expressions

This arose from an assignment about binary trees. I took it as an
opportunity to learn a bit about compilers/interpreters.

### The Language

####Operators:

m & n --- m + n

m % n --- m & n

m ? n --- max(m,n)

#### Grammar

Whitespace insensitive. Each non-integer argument to an operator requires parentheses.
