select c_custkey, l_extendedprice, l_discount, c_acctbal, c_phone, c_comment
from customer, orders, lineitem
where c_custkey = o_custkey
    AND l_orderkey = o_orderkey
    AND o_orderdate >= date '1993-10-01'
    AND o_orderdate < date '1994-01-01'
    AND l_returnflag = 'R'
