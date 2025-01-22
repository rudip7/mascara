# Mascara: Disclosure-Compliant Query Answering

This repository provides a prototype of Mascara, a middleware for specifying and enforcing data disclosure policies. Mascara extends traditional access control mechanisms with data masking to support partial disclosure of sensitive data. This allows data officers to define anonymization-based policies to comply with data protection regulations, such as the European Union’s General Data Protection Regulation (GDPR) and the California Consumer Privacy Act (CCPA). Further, Mascara also allows context-based data masking, i.e., it allows specifying masking of attributes depending on which combination of attributes are being accessed. To this end, we propose a utility estimator, which estimates the similarity between the user and modified queries. Our utility estimator enables the modification of a user query into a disclosure-compliant one with the best information quality.

### Features:
- Access control policies using masking functions to anonymize sensitive data.
- Query modification to rewrite user queries into disclosure-compliant queries. 
- Connection via JDBC to the underlying database (currently only available for PostgreSQL databases).
- Collection of data masking function implementations as UDFs.
- Utility estimation of anonymized data to find the disclosure-compliant query that preserves the highest utility while complying with defined disclosure policies.


### Set-up

Mascara, in its current state, is prepared to work with PostgreSQL. Technically, it should work with any database with a JDBC connection, but this has not been tested yet.
You must initialize a TPC-H dataset in your PostgreSQL database to run the examples and experiments in our SIGMOD paper. We recommend using [tpch-pgsql](https://github.com/Data-Science-Platform/tpch-pgsql).

Once this is done, run the SQL script at ```mascara/src/main/resources/maskingFunctions/masking.sql```. This file contains a collection of masking functions implemented as UDF in the plpgsql language.

Now, we can define the disclosure policies to protect sensitive data. This is done by running the scrip in ```mascara/src/main/resources/policies/tpch/sigmod_policies.sql```. Note that the policies are defined as materialized views for the initial prototype. In the future, we will add a parser that allows data officers to define disclosure policies using the same policy definition language as defined in our publications. Finally, Mascara needs to be aware of the current policies. The list containing all policy names is located at ```mascara/src/main/java/de/tub/dima/mascara/policies/PoliciesCatalog.java```. Please update this list if you plan to create your own policies.

All our experiments are located in the ```mascara/src/main/java/de/tub/dima/mascara/examples/``` package. We recommend starting with ```mascara/src/main/java/de/tub/dima/mascara/examples/Playground.java``` as this is the simplest example.

For any problem don't hesitate to contact me: 
Rudi Poepsel-Lemaitre
[r.poepsellemaitre@tu-berlin.de](mailto:r.poepsellemaitre@tu-berlin.de)

### Publications
#### Disclosure-Compliant Query Answering (SIGMOD 2025)

**Abstract:**  
In today’s data-driven world, organizations face increasing pressure to comply with data disclosure policies, which require data masking measures and robust access control mechanisms. This paper presents Mascara, a middleware for specifying and enforcing data disclosure policies. Mascara extends traditional access control mechanisms with data masking to support partial disclosure of sensitive data. We introduce data masks to specify disclosure policies flexibly and intuitively and propose a query modification approach to rewrite user queries into disclosure-compliant ones. We present a utility estimation framework to estimate the information loss of masked data based on relative entropy, which Mascara leverages to select the disclosure-compliant query that minimizes information loss. Our experimental evaluation shows that Mascara effectively chooses the best disclosure-compliant query with a success rate exceeding 90%, ensuring users get data with the lowest possible information loss. Additionally, Mascara’s overhead compared to normal execution without data protection is negligible, staying lower than 300ms even for extreme scenarios with hundreds of possible disclosure-compliant queries.

- Paper: [Disclosure-Compliant Query Answering](https://dl.acm.org/doi/10.1145/3698808)

- BibTeX citation:
```
@article{10.1145/3698808,
author = {Poepsel-Lemaitre, Rudi and Beedkar, Kaustubh and Markl, Volker},
title = {Disclosure-Compliant Query Answering},
year = {2024},
issue_date = {December 2024},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
volume = {2},
number = {6},
url = {https://doi.org/10.1145/3698808},
doi = {10.1145/3698808},
journal = {Proc. ACM Manag. Data},
month = dec,
articleno = {233},
numpages = {28},
keywords = {access control, data masking, information loss, privacy}
}
```

Acknowledgements: This work has received funding from theGerman Federal Ministry for Education and Research as BIFOLD—Berlin Institute for the Foundations of Learning and Data (ref. 01IS18025A and ref. 01IS18037A).

