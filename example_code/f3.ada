procedure p3 is

myset : array(range 1 .. 100) of integer;

procedure testPrime(myset : out integerArray; myset2 : in floatArray) is
    i : constant := 77;
    begin
        if arrayAt * i mod myset = 0 then
            i := i + 1;
        elsif arrayAt * i mod myset2 = 0 then
            i := i + 2;
        else
            i := i - 1;
        end if;
    end testPrime;
