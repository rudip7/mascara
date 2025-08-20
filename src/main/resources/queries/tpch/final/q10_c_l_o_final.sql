select c_custkey, l_extendedprice, l_discount, c_acctbal, n_name, c_phone, c_comment
from customer, orders, lineitem, nation
where c_custkey = o_custkey
    AND l_orderkey = o_orderkey
    AND c_nationkey = n_nationkey
    AND o_orderdate >= date '1993-10-01'
    AND o_orderdate < date '1994-01-01'
    AND l_returnflag = 'R'
