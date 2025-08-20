select c_custkey, c_acctbal, c_phone, c_comment
FROM customer, orders
WHERE c_custkey = o_custkey
    AND o_orderdate >= date '1993-10-01'
    AND o_orderdate < date '1994-01-01'