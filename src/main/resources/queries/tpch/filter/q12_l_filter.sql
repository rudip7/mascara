select l_orderkey, l_suppkey, l_quantity, l_extendedprice,  l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipmode
from lineitem
where l_shipmode in ('MAIL', 'SHIP')
  AND l_commitdate < l_receiptdate
  AND l_shipdate < l_commitdate
  AND l_receiptdate >= date '1994-01-01'
  AND l_receiptdate < date '1995-01-01'
