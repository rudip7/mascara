select o_orderdate, o_shippriority
FROM customer, orders
WHERE c_custkey = o_custkey
    AND c_mktsegment = 'AUTOMOBILE'
    AND o_orderdate < date '1995-03-15'