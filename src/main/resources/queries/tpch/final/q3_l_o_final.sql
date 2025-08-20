select l_orderkey, l_extendedprice, l_discount, o_orderdate, o_shippriority
from orders, lineitem
where o_orderkey = l_orderkey
    and o_orderdate < date '1995-03-15'
    and l_shipdate > date '1995-03-15'