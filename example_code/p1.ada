procedure p1 is
type complexNumber is
    array (range 1 .. 100, range -1 .. -100 ) of coupleImage ;

procedure calcNorm(num1, num2 : in out complexNumber) is
    begin
    null;
    i := num1 ** 2 + num2 ** 2;
    while i mod num1 < num2 loop
        j := maxNorm;
    end loop;
    end calcNorm;

begin
calcNorm(foo, bar);
end;