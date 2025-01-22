# Mascara: Disclosure-Compliant Query Answering

This repository provides a prototype of Mascara, a middleware for specifying and enforcing data disclosure policies. Mascara extends traditional access control mechanisms with data masking to support partial disclosure of sensitive data. This allows for data officers to define anonymization-based policies to comply with data protection regulations, such as European Union’s General Data Protection Regulation (GDPR) and the California Consumer Privacy Act (CCPA). Further, Mascara also allows context-based data masking, i.e., it allows specifying masking of attributes depending on which combination of attributes are being accessed. To this end, we propose a utility estimator, which estimates the similarity between the user and modified queries. Our utility estimator enables the modification of a user query into a disclosure-compliant one with the best information quality.

### Features:
- Access control policies using masking functions to anonymize sensitive data.
- Query modification to rewrite user queries into disclosure-compliant queries. 
- Connection via JDBC to the underlying database (currently only available for PostgreSQL databases).
- Collection of data masking function implementations as UDFs.
- Utility estimation of anonymized data to find the disclosure-compliant query that preserves the highest utility while complying to all defined disclousire policies.


### Set-up

Mascara in it's current state is prepared to work with PostgreSQL. Technically it should work with any database with JDBC connection but this has not been tested yet.
To run the examples and experiments in our SIGMOD paper, you must initiallize  


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

