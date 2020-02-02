procedure p4 is

type color is (red, green, blue);

mycolor : color;

begin
    if mycolor = red then
        print(red);
    elsif mycolor = green then
        print(green);
    elsif mycolor = blue then
        print(blue);
    else
        null;
        exit;
    end if;
end p4;