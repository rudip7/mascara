SELECT c_custkey, c_nationkey, c_phone, c_acctbal, c_mktsegment, c_comment, o_orderkey, o_custkey, o_orderdate, o_orderpriority, o_shippriority, o_comment
FROM customer, orders
WHERE c_custkey = o_custkey
    AND o_orderdate >= date '1994-01-01'
    AND o_orderdate < date '1995-01-01'