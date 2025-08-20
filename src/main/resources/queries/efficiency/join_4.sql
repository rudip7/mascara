select l_extendedprice, l_discount
from customer, orders, lineitem, supplier
where c_custkey = o_custkey
  AND l_orderkey = o_orderkey
  AND l_suppkey = s_suppkey
  AND c_nationkey = s_nationkey
    AND o_orderdate >= date '1994-01-01'
    AND o_orderdate < date '1995-01-01'
