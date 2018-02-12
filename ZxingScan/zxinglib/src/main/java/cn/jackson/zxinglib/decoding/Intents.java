/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.jackson.zxinglib.decoding;

/**
 * This class provides the constants to use when sending an Intent to Barcode Scanner.
 * These strings are effectively API and cannot be changed.
 */
public final class Intents {
    private Intents() {
    }

    public static final class Scan {
        /**
         * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
         * the results.
         */
        public static final String ACTION = "com.google.zxing.client.android.SCAN";

        /**
         * By default, sending Scan.ACTION will decode all barcodes that we understand. However it
         * may be useful to limit scanning to certain formats. Use Intent.putExtra(MODE, value) with
         * one of the values below ({@link #PRODUCT_MODE}, {@link #ONE_D_MODE}, {@link #QR_CODE_MODE}).
         * Optional.
         * <p>
         * Setting this is effectively shorthnad for setting explicit formats with {@link #SCAN_FORMATS}.
         * It is overridden by that setting.
         */
        public static final String MODE = "SCAN_MODE";

        /**
         * Comma-separated list of formats to scan for. The values must match the names of
         * {@link com.google.zxing.BarcodeFormat}s, such as {@link com.google.zxing.BarcodeFormat#EAN_13}.
         * Example: "EAN_13,EAN_8,QR_CODE"
         * <p>
         * This overrides {@link #MODE}.
         */
        public static final String SCAN_FORMATS = "SCAN_FORMATS";

        /**
         * @see com.google.zxing.DecodeHintType#CHARACTER_SET
         */
        public static final String CHARACTER_SET = "CHARACTER_SET";

        /**
         * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
         * prices, reviews, etc. for products.
         */
        public static final String PRODUCT_MODE = "PRODUCT_MODE";

        /**
         * Decode only 1D barcodes (currently UPC, EAN, Code 39, and Code 128).
         */
        public static final String ONE_D_MODE = "ONE_D_MODE";

        /**
         * Decode only QR codes.
         */
        public static final String QR_CODE_MODE = "QR_CODE_MODE";

        /**
         * Decode only Data Matrix codes.
         */
        public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";

    }
}
