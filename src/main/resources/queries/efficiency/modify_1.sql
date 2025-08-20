SELECT c_custkey, c_nationkey, c_phone, c_acctbal, c_mktsegment
FROM customer
where c_acctbal > 1000.0 and
    c_mktsegment = 'AUTOMOBILE' and
    c_nationkey = 10