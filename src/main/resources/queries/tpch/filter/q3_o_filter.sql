select o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment
from orders
where o_orderdate < date '1995-03-15'