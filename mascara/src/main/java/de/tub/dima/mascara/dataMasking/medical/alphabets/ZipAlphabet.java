package de.tub.dima.mascara.dataMasking.medical.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;

import java.util.HashMap;

public class ZipAlphabet extends Alphabet {
    private long idx = 0;
    public ZipAlphabet() {

        this.alphabet = new HashMap<>();
        // For lazy people
        // addRange(0, 999999);
        // Detailed list of zip codes in germany
        addRange(1067, 4889);
        addRange(3042, 3253);
        addRange(6108, 6928);
        addRange(7318, 7989);
        addRange(80331, 97909);
        addRange(10115, 14532);
        addRange(14461, 17326);
        addRange(17033, 19417);
        addRange(20038, 21149);
        addRange(21217, 21789);
        addRange(22041, 22769);
        addRange(22844, 25999);
        addRange(26121, 38729);
        addRange(27568, 27580);
        addRange(28195, 28779);
        addRange(32049, 33829);
        addRange(34117, 37299);
        addRange(38820, 39649);
        addRange(40196, 54585);
        addRange(49074, 49849);
        addRange(54290, 57648);
        addRange(58084, 59969);
        addRange(60306, 63699);
        addRange(64283, 65936);
        addRange(66041, 66839);
        addRange(66849, 67829);
        addRange(68131, 79879);
        addRange(88212, 89198);
        addRange(98527, 99998);
    }

    private void addRange(int start, int end) {
        for (int i = start; i <= end; i++) {
            String formattedZIPCode = String.format("%05d", i);
            alphabet.put(formattedZIPCode, idx);
            idx++;
        }
    }
}
