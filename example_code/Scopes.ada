procedure TEST is


    STACK : STACK_TYPE;

    procedure PUSH(DATA : in ELEMENT; STACK : in out STACK_TYPE) is
         DATA : constant := expr;
         begin
         STACKTOP := STACKTOP + 1;
         DATA := 3;
         STACKA(STACKTOP) := DATA;
         end PUSH;


    begin
    NEW_STACK(STACK);
    PUSH(20, STACK);
    end TEST;
