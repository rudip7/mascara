select l_extendedprice, l_extendedprice, l_extendedprice, l_extendedprice, l_extendedprice, l_extendedprice, l_extendedprice, l_extendedprice
from customer, orders, lineitem
where c_custkey = o_custkey
  AND l_orderkey = o_orderkey
    AND o_orderdate >= date '1994-01-01'
    AND o_orderdate < date '1995-01-01'
