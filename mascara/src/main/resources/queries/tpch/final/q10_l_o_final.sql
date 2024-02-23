select l_extendedprice, l_discount
from orders, lineitem
where o_orderkey = l_orderkey
    and o_orderdate >= date '1993-10-01'
    AND o_orderdate < date '1994-01-01'
    and l_returnflag = 'R'