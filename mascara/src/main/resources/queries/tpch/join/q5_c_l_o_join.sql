select c_custkey, c_nationkey, c_phone, c_acctbal, c_mktsegment, c_comment, o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment, l_orderkey, l_suppkey, l_quantity, l_extendedprice,  l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipmode
from customer, orders, lineitem
where c_custkey = o_custkey
  AND l_orderkey = o_orderkey
    AND o_orderdate >= date '1994-01-01'
    AND o_orderdate < date '1995-01-01'
