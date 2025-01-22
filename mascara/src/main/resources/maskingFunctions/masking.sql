CREATE OR REPLACE FUNCTION bucketize(input_num NUMERIC, b_size NUMERIC, num_digits_left INTEGER DEFAULT 6, num_digits_right INTEGER DEFAULT 2)
    RETURNS TEXT
AS $$
DECLARE
    lower_bound NUMERIC;
    upper_bound NUMERIC;
    format_string TEXT;
BEGIN
    format_string := REPEAT(9::text, num_digits_left) || '.' || REPEAT(9::text, num_digits_right);

    lower_bound := floor(input_num / b_size) * b_size;
    upper_bound := lower_bound + b_size;

    RETURN '[' || to_char(lower_bound, format_string) || ',' || to_char(upper_bound, format_string) || ')';
END;
$$ LANGUAGE plpgsql;

-- Returning ranges
CREATE OR REPLACE FUNCTION bucketize(input_num NUMERIC, b_size NUMERIC)
    RETURNS NUMRANGE
AS $$
DECLARE
    lower_bound NUMERIC;
    upper_bound NUMERIC;
BEGIN
    IF input_num < 0.0 THEN
        upper_bound := input_num - (input_num % b_size);
        lower_bound := input_num - b_size - (input_num % b_size);
    ELSE
        lower_bound := input_num - (input_num % b_size);
        upper_bound := input_num + b_size - (input_num % b_size);
    END IF;
    RETURN NUMRANGE(lower_bound, upper_bound);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION bucketize_low(input_num NUMERIC, b_size NUMERIC)
    RETURNS NUMERIC
AS $$
BEGIN
    IF input_num < 0.0 THEN;
        RETURN input_num - b_size - (input_num % b_size);
    ELSE
        RETURN input_num - (input_num % b_size);
    END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION to_range(range_str TEXT)
    RETURNS NUMRANGE
AS $$
BEGIN
    RETURN NUMRANGE(range_str);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_noise_date(input_date DATE, level TEXT, noise INT)
    RETURNS DATE
    LANGUAGE plpgsql
AS $$
DECLARE
    noise2 INT;
    min_date DATE;
    random_date DATE;
BEGIN
    noise2 := ABS(noise);

    IF level = 'DAYS' THEN
        min_date := input_date - noise2;
    ELSIF level = 'WEEKS' THEN
        min_date := input_date - (noise2 * 7);
    ELSIF level = 'MONTHS' THEN
        min_date := input_date - INTERVAL '1' MONTH * noise2;
    ELSIF level = 'YEARS' THEN
        min_date := input_date - INTERVAL '1' YEAR * noise2;
    ELSE
        RAISE EXCEPTION 'For the MF NOISE_DATE, level should be either ''DAYS'', ''WEEKS'', ''MONTHS'', or ''YEARS'' and was %', level;
    END IF;

    random_date := min_date + INTERVAL '1' DAY * (random() * 2 * noise2);

    RETURN random_date;
END;
$$;

CREATE OR REPLACE FUNCTION add_noise_date(input_date DATE, level INT, noise INT)
    RETURNS DATE
    LANGUAGE plpgsql
AS $$
DECLARE
    noise2 INT;
    min_date DATE;
    random_date DATE;
BEGIN
    noise2 := ABS(noise);

    IF level = 1 THEN
        min_date := input_date - noise2;
    ELSIF level = 2 THEN
        min_date := input_date - (noise2 * 7);
    ELSIF level = 3 THEN
        min_date := input_date - INTERVAL '1' MONTH * noise2;
    ELSIF level = 4 THEN
        min_date := input_date - INTERVAL '1' YEAR * noise2;
    ELSE
        RAISE EXCEPTION 'For the MF NOISE_DATE, level should be either ''DAYS'', ''WEEKS'', ''MONTHS'', or ''YEARS'' and was %', level;
    END IF;

    random_date := min_date + INTERVAL '1' DAY * (random() * 2 * noise2);

    RETURN random_date;
END;
$$;



CREATE OR REPLACE FUNCTION generalize_date(input_date DATE, granularity TEXT)
    RETURNS DATE
AS $$
BEGIN
    IF granularity = 'MONTH' THEN
        RETURN DATE_TRUNC('MONTH', input_date);
    ELSIF granularity = 'YEAR' THEN
        RETURN DATE_TRUNC('YEAR', input_date);
    ELSE
        RETURN input_date;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generalize_date(input_date DATE, granularity INT)
    RETURNS DATE
AS $$
BEGIN
    IF granularity = 3 THEN
        RETURN DATE_TRUNC('MONTH', input_date);
    ELSIF granularity = 4 THEN
        RETURN DATE_TRUNC('YEAR', input_date);
    ELSE
        RETURN input_date;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_relative_noise(val NUMERIC, rel_noise NUMERIC, n INT DEFAULT 1)
    RETURNS NUMERIC
    LANGUAGE plpgsql
AS $$
DECLARE
    random_val NUMERIC;
BEGIN
    IF NOT (0.0 < rel_noise AND rel_noise < 1.0) THEN
        RAISE EXCEPTION 'rel_noise must be in the range (0.0, 1.0)';
    END IF;

    random_val := (val - (val * rel_noise)) + (random() * (2 * val * rel_noise));
    RETURN ROUND(random_val, n);
END;
$$;

CREATE OR REPLACE FUNCTION add_absolute_noise(val NUMERIC, noise NUMERIC, n INT DEFAULT 2)
    RETURNS NUMERIC
    LANGUAGE plpgsql
AS $$
DECLARE
    random_val NUMERIC;
BEGIN
    random_val := (val - noise) + (random() * (2 * noise));
    RETURN ROUND(random_val, n);
END;
$$;

CREATE OR REPLACE FUNCTION add_laplace_noise(val NUMERIC, sensitivity NUMERIC, epsilon NUMERIC, n INT DEFAULT 2)
    RETURNS NUMERIC
    LANGUAGE plpgsql
AS $$
DECLARE
    scale_factor NUMERIC;
    laplace_noise NUMERIC;
    random_val NUMERIC;
BEGIN
    -- Calculate the scale factor for Laplace noise
    scale_factor := sensitivity / epsilon;

    -- Generate Laplace noise
    laplace_noise := random() - 0.5; -- Uniform random value between -0.5 and 0.5
    laplace_noise := -SIGN(laplace_noise) * scale_factor * ln(1.0 - 2.0 * ABS(laplace_noise));

    -- Add Laplace noise to the original value
    random_val := val + laplace_noise;

    -- Ensure the result is non-negative
    IF random_val < 0 THEN
        laplace_noise := laplace_noise * -1;
        random_val := val + laplace_noise;
    END IF;

    -- Round the result to n decimal places
    RETURN ROUND(random_val, n);
END;
$$;

CREATE OR REPLACE FUNCTION blur_phone(phone TEXT)
    RETURNS TEXT AS $$
DECLARE
    blurred_phone TEXT;
BEGIN
    -- Extract the area code and last 4 digits
    blurred_phone := SUBSTRING(phone FROM 1 FOR 7) || 'XXX-XXXX';

    RETURN blurred_phone;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION blur_phone(value NUMERIC, divisor NUMERIC)
    RETURNS NUMERIC
    LANGUAGE plpgsql
AS $$
DECLARE
    factor NUMERIC;
BEGIN
    factor := value / divisor;
    RETURN floor(factor) * divisor;
END;
$$;

CREATE OR REPLACE FUNCTION suppress(value ANYELEMENT)
    RETURNS TEXT
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN 'XXXXX';
END;
$$;


CREATE OR REPLACE FUNCTION generalize_number(value NUMERIC, divisor NUMERIC)
    RETURNS NUMERIC
    LANGUAGE plpgsql
AS $$
DECLARE
    factor NUMERIC;
BEGIN
    factor := value / divisor;
    RETURN floor(factor) * divisor;
END;
$$;


CREATE OR REPLACE FUNCTION get_range_low(int_range int4range)
    RETURNS INT
AS $$
BEGIN
    RETURN lower(int_range);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_range_high(int_range int4range)
    RETURNS INT
AS $$
BEGIN
    RETURN upper(int_range);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_range_midpoint(bucket_range NUMRANGE)
    RETURNS NUMERIC
AS $$
BEGIN
    RETURN (lower(bucket_range) + upper(bucket_range)) / 2;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION blur_date(input_date DATE, nFields INT)
    RETURNS TEXT
AS $$
DECLARE
    date_str TEXT;
    parts TEXT[];
BEGIN
    date_str := TO_CHAR(input_date, 'YYYY-MM-DD');
    parts := string_to_array(date_str, '-');
    IF nFields >= LENGTH(date_str) THEN
        RETURN RPAD('X', LENGTH(date_str), 'X');
    ELSE
        parts[1] := LEFT(parts[1], GREATEST(LENGTH(parts[1])-GREATEST(nFields-LENGTH(parts[3])-LENGTH(parts[2]),0),0)) ||RPAD('X', LEAST(LENGTH(parts[1]),GREATEST(nFields-LENGTH(parts[3])-LENGTH(parts[2]),0)), 'X');
        parts[2] := LEFT(parts[2], GREATEST(LENGTH(parts[2])-GREATEST(nFields-LENGTH(parts[3]),0),0)) ||RPAD('X', LEAST(LENGTH(parts[2]),GREATEST(nFields-LENGTH(parts[3]),0)), 'X');
        parts[3] := LEFT(parts[3], GREATEST(LENGTH(parts[3]) - nFields,0)) ||RPAD('X', LEAST(LENGTH(parts[3]),nFields), 'X');
        RETURN array_to_string(parts, '-');
    END IF;
END;
$$ LANGUAGE plpgsql;

