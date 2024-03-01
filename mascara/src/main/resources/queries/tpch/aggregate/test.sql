select l_returnflag, l_linestatus, (l_extendedprice*(1-l_discount)) AS disc_price
from lineitem
where l_shipdate <= date '1998-09-02'
