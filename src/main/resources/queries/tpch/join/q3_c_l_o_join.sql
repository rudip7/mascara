select c_custkey, c_nationkey, c_phone, c_acctbal, c_mktsegment, c_comment, o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment, l_orderkey, l_suppkey, l_quantity, l_extendedprice,  l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipmode
from customer, orders, lineitem
where o_orderkey = l_orderkey
    and c_custkey = o_custkey
    AND c_mktsegment = 'AUTOMOBILE'
    and o_orderdate < date '1995-03-15'
    and l_shipdate > date '1995-03-15'