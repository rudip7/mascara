select *
from public."Patient" as p, public."Masked_low" as ml
where p.id = ml.id