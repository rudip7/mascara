select BUCKETIZE_AGE(age, 5) as age,
       gender,
       GENERALIZE_DIAGNOSIS(diagnosis, 1) as diagnosis,
       BLUR_ZIP(zip, 2) as zip
from public."Patient"