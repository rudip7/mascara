select l_returnflag, l_linestatus, sum(l_extendedprice) AS sum_base_price,
       sum(l_extendedprice*(1-l_discount)) AS sum_disc_price
from lineitem
where l_shipdate <= date '1998-09-02'
group by l_returnflag, l_linestatus
