select c_custkey, c_nationkey, c_phone, c_acctbal, c_mktsegment, c_comment, o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment, l_orderkey, l_suppkey, l_quantity, l_extendedprice,  l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipmode, n_nationkey, n_name, n_regionkey, n_comment
from customer, orders, lineitem, nation
where c_custkey = o_custkey
    AND l_orderkey = o_orderkey
    AND c_nationkey = n_nationkey
    AND o_orderdate >= date '1993-10-01'
    AND o_orderdate < date '1994-01-01'
    AND l_returnflag = 'R'
