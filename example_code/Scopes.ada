procedure TEST is

    type ELEMENT is INTEGER;

    MIN_ARRAY : constant := 1;

    MAX_ARRAY : constant := 10;

    type ARRAY_INDEX is range MIN_ARRAY..MAX_ARRAY;

    type ARRAY_TYPE is array(ARRAY_INDEX) of ELEMENT;

    MIN_STACK : constant := MIN_ARRAY - 1;

    MAX_STACK : constant := MAX_ARRAY;

    type STACK_POINTER is range MIN_STACK..MAX_STACK;

    type STACK_TYPE is
         record
         A : ARRAY_TYPE;
         TOP : STACK_POINTER;
         end record;

    STACK : STACK_TYPE;

    procedure PUSH(DATA : in ELEMENT; STACK : in out STACK_TYPE) is
         begin
         STACK.TOP := STACK.TOP + 1;
         STACK.A(STACK.TOP) := DATA;
         end PUSH;

    procedure POP(DATA : out ELEMENT; STACK : in out STACK_TYPE) is
         begin
         DATA := STACK.A(STACK.TOP);
         STACK.TOP := STACK.TOP - 1;
         end POP;

    procedure NEW_STACK(STACK : out STACK_TYPE) is
         begin
         STACK.TOP := STACK_MIN;
         end;

    procedure EMPTY_STACK(STACK : in STACK_TYPE; EMPTY : out BOOLEAN) is
         begin
         EMPTY := STACK.TOP = STACK_MIN;
         end EMPTY_STACK;

    procedure FULL_STACK(STACK : in STACK_TYPE; FULL : out BOOLEAN) is
         begin
         FULL := STACK.TOP = MAX_STACK;
         end FULL_STACK;

    begin
    NEW_STACK(STACK);
    PUSH(20, STACK);
    end TEST;
_
