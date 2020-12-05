begin
  integer BKs_669$kk#####@@@@@@@NSkkkkkkkkk;
  integer function F(n);
    begin
      integer n;
      if n<=0 then f:=1
      else F:=n*F(n-1)
    end;
  read(m);
  k:F(m);
  write(k)
end