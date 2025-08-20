select l_orderkey, l_extendedprice, l_discount, o_orderdate, o_shippriority
from customer, orders, lineitem
where o_orderkey = l_orderkey
    and c_custkey = o_custkey
    AND c_mktsegment = 'AUTOMOBILE'
    and o_orderdate < date '1995-03-15'
    and l_shipdate > date '1995-03-15'