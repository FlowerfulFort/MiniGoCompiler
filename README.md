# MiniGoCompiler
Compile minigo(MiniGo.g4) to jvm bytecode.

## Feature
- Pretty-print for minigo source file(not perfect yet).
- Compile minigo source to jvm bytecode for compiling with jasmin.

## Not Implemented
- Pretty-print for some grammar was not implemented.
- Compile for array variable was not implemented.

## How to use
`java -jar mgoc.jar [option] [target] [output]`

### Options
- `-p, -P`: Pretty-print minigo to minigo.
- `-b, -B`: Compile minigo to jvm bytecode.
