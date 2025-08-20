select o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment
from orders
where o_orderdate >= date '1994-01-01'
  AND o_orderdate < date '1995-01-01'
