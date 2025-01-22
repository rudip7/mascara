-- STATISTICS LEVEL 100
ALTER TABLE lineitem ALTER column l_extendedprice SET STATISTICS 100;
ALTER TABLE lineitem ALTER column l_discount SET STATISTICS 100;
ALTER TABLE lineitem ALTER column l_shipdate SET STATISTICS 100;
ANALYZE lineitem;

ALTER TABLE l_p1 ALTER column l_extendedprice SET STATISTICS 100;
ALTER TABLE l_p1 ALTER column l_discount SET STATISTICS 100;
ANALYZE l_p1;

ALTER TABLE l_p2 ALTER column l_extendedprice SET STATISTICS 100;
ALTER TABLE l_p2 ALTER column l_shipdate SET STATISTICS 100;
ANALYZE l_p2;

ALTER TABLE l_p3 ALTER column l_discount SET STATISTICS 100;
ALTER TABLE l_p3 ALTER column l_shipdate SET STATISTICS 100;
ANALYZE l_p3;

-- STATISTICS LEVEL 1000
ALTER TABLE lineitem ALTER column l_extendedprice SET STATISTICS 1000;
ALTER TABLE lineitem ALTER column l_discount SET STATISTICS 1000;
ALTER TABLE lineitem ALTER column l_shipdate SET STATISTICS 1000;
ANALYZE lineitem;

ALTER TABLE l_p1 ALTER column l_extendedprice SET STATISTICS 1000;
ALTER TABLE l_p1 ALTER column l_discount SET STATISTICS 1000;
ANALYZE l_p1;

ALTER TABLE l_p2 ALTER column l_extendedprice SET STATISTICS 1000;
ALTER TABLE l_p2 ALTER column l_shipdate SET STATISTICS 1000;
ANALYZE l_p2;

ALTER TABLE l_p3 ALTER column l_discount SET STATISTICS 1000;
ALTER TABLE l_p3 ALTER column l_shipdate SET STATISTICS 1000;
ANALYZE l_p3;

-- STATISTICS LEVEL 10000
ALTER TABLE lineitem ALTER column l_extendedprice SET STATISTICS 10000;
ALTER TABLE lineitem ALTER column l_discount SET STATISTICS 10000;
ALTER TABLE lineitem ALTER column l_shipdate SET STATISTICS 10000;
ANALYZE lineitem;

ALTER TABLE l_p1 ALTER column l_extendedprice SET STATISTICS 10000;
ALTER TABLE l_p1 ALTER column l_discount SET STATISTICS 10000;
ANALYZE l_p1;

ALTER TABLE l_p2 ALTER column l_extendedprice SET STATISTICS 10000;
ALTER TABLE l_p2 ALTER column l_shipdate SET STATISTICS 10000;
ANALYZE l_p2;

ALTER TABLE l_p3 ALTER column l_discount SET STATISTICS 10000;
ALTER TABLE l_p3 ALTER column l_shipdate SET STATISTICS 10000;
ANALYZE l_p3;

