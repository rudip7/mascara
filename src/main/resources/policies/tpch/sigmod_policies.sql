-- POLICIES FOR CUSTOMER TABLE
CREATE MATERIALIZED VIEW c_p1 AS
SELECT c_custkey, c_nationkey, suppress(c_phone) as c_phone, bucketize_low(c_acctbal, 250.0) as c_acctbal_stat, bucketize(c_acctbal, 250.0) as c_acctbal, c_mktsegment, c_comment
FROM customer;

CREATE MATERIALIZED VIEW c_p2 AS
SELECT c_custkey, c_nationkey, blur_phone(c_phone) as c_phone, bucketize_low(c_acctbal, 500.0) as c_acctbal_stat, bucketize(c_acctbal, 500.0) as c_acctbal, c_mktsegment, c_comment
FROM customer;

-- POLICIES FOR LINEITEM TABLE
CREATE MATERIALIZED VIEW l_p1 AS
SELECT l_orderkey, l_suppkey, l_quantity, bucketize_low(l_extendedprice, 250.0) as l_extendedprice_stat, bucketize(l_extendedprice, 250.0) as l_extendedprice, add_absolute_noise(l_discount, 0.05) as l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipmode
FROM lineitem;

CREATE MATERIALIZED VIEW l_p2 AS
SELECT l_orderkey, l_suppkey, l_quantity, add_relative_noise(l_extendedprice, 0.1) as l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus, add_noise_date(l_shipdate, 'DAYS', 30) as l_shipdate, l_commitdate, l_receiptdate, l_shipmode
FROM lineitem;

CREATE MATERIALIZED VIEW l_p3 AS
SELECT l_orderkey, l_suppkey, l_quantity, l_extendedprice, bucketize_low(l_discount, 0.05) as l_discount_stat, bucketize(l_discount, 0.05) as l_discount, l_tax, l_returnflag, l_linestatus, generalize_date(l_shipdate, 'MONTH') as l_shipdate, l_commitdate, l_receiptdate, l_shipmode
FROM lineitem;

-- POLICIES FOR ORDERS TABLE
CREATE MATERIALIZED VIEW o_p1 AS
SELECT o_orderkey, o_custkey, add_noise_date(o_orderdate, 'DAYS', 10) as o_orderdate, o_orderpriority, suppress(o_shippriority) as o_shippriority, o_comment
FROM orders;

CREATE MATERIALIZED VIEW o_p2 AS
SELECT o_orderkey, o_custkey, generalize_date(o_orderdate, 'MONTH') as o_orderdate, o_orderpriority, o_shippriority, o_comment
FROM orders;

-- EXTRA TABLES WITHOUT MASKING
create materialized view n as
select *
from nation;

create materialized view r as
select *
from region;

create materialized view s as
select *
from supplier;



