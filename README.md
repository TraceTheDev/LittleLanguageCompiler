# LittleCompiler

A Java-based implementation of a front-end compiler for a simplified **Little programming language**, featuring syntax parsing with ANTLR, symbol table generation, intermediate representation (IR) creation, and pseudo-assembly (tiny code) output. This project showcases compiler design concepts including lexical analysis, parsing, semantic checks, and code generation.

Ideal for learning or teaching compiler design fundamentals using real-world tools like ANTLR4.

---

## Project Structure

```
LittleCompiler/
├── src/
│   ├── LittleLexer.java             # ANTLR-generated lexer
│   ├── LittleParser.java            # ANTLR-generated parser
│   ├── LittleParserBaseListener.java  # ANTLR-generated base listener
│   ├── LittleParserListener.java    # ANTLR-generated interface
│   └── SimpleTableBuilder.java      # Custom listener for symbol tables, IR, and tiny code
├── Little.g4                        # Grammar file for ANTLR
```

---

## Core Java Class

### `SimpleTableBuilder.java`
Handles:
- Symbol table construction across nested scopes
- Parsing and interpretation of assignments, expressions, conditions, and IO
- Generation of intermediate representation (IR) code
- Translation of IR into pseudo-assembly-like “tiny” code

**Key Features:**
- Scope-aware symbol table using stack-based maps
- Support for both `INT` and `FLOAT` types
- Expression parsing with limited binary operator support
- IR generation: `STOREI`, `STOREF`, `ADDI`, `SUBF`, etc.
- Tiny code output: `move`, `add`, `cmpi`, `sys read/write`

---

## How to Run

### Generate Lexer & Parser with ANTLR
```bash
antlr4 Little.g4
javac *.java
```

### Compile and Run
```bash
javac SimpleTableBuilder.java
java org.antlr.v4.runtime.misc.TestRig Little program -gui
```

---

## Sample Output

```
Symbol table GLOBAL
name a type INT
name b type FLOAT

;IR code
STOREI a $T1
STOREF b $T2
ADDI $T1 $T2 $T3
STOREI $T3 a

;Tiny code
move a r0
move b r1
addi r0 r1
move r0 a
```

---

## Future Enhancements

- Full expression tree parsing with operator precedence
- Add support for string literals and function declarations
- Extend IR generation to support control flow and logical ops
- Add error reporting for type mismatches or undeclared variables

---

## License
This project is licensed for personal, non-commercial use only. Redistribution, resale, or modification is prohibited without written permission from the author.  
See the [LICENSE] file for full details.

---

## Author  
**Trace Davis**  
- GitHub: [Trace0727](https://github.com/Trace0727)  
- LinkedIn: [Trace Davis](https://www.linkedin.com/in/trace-d-926380138/)
