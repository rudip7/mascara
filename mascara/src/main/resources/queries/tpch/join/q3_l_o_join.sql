select o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment, l_orderkey, l_suppkey, l_quantity, l_extendedprice,  l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipmode
from orders, lineitem
where o_orderkey = l_orderkey
    and o_orderdate < date '1995-03-15'
    and l_shipdate > date '1995-03-15'