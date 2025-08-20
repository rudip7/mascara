select l_returnflag, l_extendedprice, sum(l_extendedprice*(1-l_discount)) AS disc_price
from lineitem
where l_shipdate <= date '1998-09-02'
group by l_returnflag, l_extendedprice
