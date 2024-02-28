select l_returnflag, l_linestatus, l_quantity, l_extendedprice, l_discount, l_tax
from lineitem
where l_shipdate <= date '1998-09-02'
