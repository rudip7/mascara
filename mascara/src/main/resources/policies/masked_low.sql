select id,
       name,
       gender,
       BUCKETIZE_AGE(age, 5) as age,
       ADD_RELATIVE_NOISE(height, 0.05) as height,
       weight,
       GENERALIZE_DIAGNOSIS(diagnosis, 1) as diagnosis,
       BLUR_ZIP(zip, 2) as zip,
       BLUR_PHONE(phone, 3) as phone
from public."Patient"