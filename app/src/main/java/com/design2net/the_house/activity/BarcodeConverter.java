package com.design2net.the_house.activity;

public class BarcodeConverter {
    private final int SHORT_UPC_LENGTH = 8;
    private String barcode;

    public BarcodeConverter(String barcode) {
        this.barcode = barcode;
    }

    public String barcode() {
        String result = barcode;

        if(isShort())
            result = convertToShortUPC(result);

        return removeCheckDigit(result);
    }

    public boolean isShort() {
        return barcode.length() == SHORT_UPC_LENGTH;
    }

    private String convertToShortUPC(String upc) {
        if(upc.charAt(0) != '0' && upc.charAt(0) != '1') {
            return "";
        }
        else if(upc.charAt(6) == '0' || upc.charAt(6) == '1' || upc.charAt(6) == '2') {
            return upc.substring(0, 3) + upc.charAt(6) + "0000" + upc.substring(3, 6) + upc.charAt(7);
        }
        else if(upc.charAt(6) == '3') {
            return upc.substring(0, 4) + "00000" + upc.substring(4, 6) + upc.charAt(7);
        }
        else if(upc.charAt(6) == '4') {
            return upc.substring(0, 5) + "00000" + upc.charAt(5) + upc.charAt(7);
        }
        else if(upc.charAt(6) == '5' || upc.charAt(6) == '6' || upc.charAt(6) == '7' || upc.charAt(6) == '8' || upc.charAt(6) == '9') {
            return upc.substring(0, 6) + "0000" + upc.charAt(6) + upc.charAt(7);
        }

        return upc;
    }

    private String removeCheckDigit(String barcode) {
        if(barcode.isEmpty())
            return barcode;

        return barcode.substring(0, barcode.length() - 1);
    }

}
