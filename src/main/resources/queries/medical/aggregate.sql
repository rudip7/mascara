select gender, zip, avg(height) as avg_height, max(weight) as max_weight
from public."Patient"
where age > 57 and age < 87
group by gender, zip