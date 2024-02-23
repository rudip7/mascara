select c_custkey, c_nationkey, c_phone, c_acctbal, c_mktsegment, c_comment, o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment, l_orderkey, l_suppkey, l_quantity, l_extendedprice,  l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipmode, n_nationkey, n_name, n_regionkey, n_comment, s_suppkey, s_name, s_address, s_phone, s_acctbal, s_comment, r_regionkey, r_name, r_comment
from customer, orders, lineitem, supplier, nation, region
where c_custkey = o_custkey
  AND l_orderkey = o_orderkey
  AND l_suppkey = s_suppkey
  AND c_nationkey = s_nationkey
  AND s_nationkey = n_nationkey
  AND n_regionkey = r_regionkey
  AND r_name = 'ASIA'
    AND o_orderdate >= date '1994-01-01'
    AND o_orderdate < date '1995-01-01'
